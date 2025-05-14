package com.unitTestGenerator.ioc;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import com.unitTestGenerator.ioc.anotations.*;
import org.reflections.Reflections;

    public class ContextIOC {

        private static ContextIOC instance;
        private  static Class<?> nainClass;
        private Map<String, Object> singletonInstances = new HashMap<>();
        private Map<String, Class<?>> registeredClasses = new HashMap<>();

        public ContextIOC() {
        }

        public static ContextIOC getInstance(){
            if(instance == null){
                instance = new ContextIOC();
            }
            return instance;
        }

        public static ContextIOC getInstance(String packge){
            instance = getInstance();
            instance.scanPackage(packge);
            return instance;
        }


        public static ContextIOC getInstance(Class<?> clazzI) {
            try {
                instance = getInstance();
                nainClass = clazzI;
                if (nainClass.isAnnotationPresent(EndebleIOC.class)) {
                    EndebleIOC anotation = nainClass.getAnnotation(EndebleIOC.class);
                    String packge = anotation.value();
                    instance.scanPackage(packge);
                } else {
                    System.out.println("Class does not have the EndebleIOC annotation.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred while trying to read the package annotation... ERROR:.."+e);
                e.printStackTrace();
                return getInstance();
            }
            return instance;
        }


        public void scanPackage(String packageName) {
            Reflections reflections = new Reflections(packageName);
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class);

            // Registrar todas las clases primero
            for (Class<?> clazz : classes) {
                register(clazz);
            }
            System.out.println("Registered classes: " + registeredClasses.keySet());
            // Crear instancias de clases Singleton después de registrar todas
            for (Class<?> clazz : registeredClasses.values()) {
                if (clazz.isAnnotationPresent(Singleton.class)) {
                    String name = clazz.getSimpleName().toLowerCase();
                    if (!singletonInstances.containsKey(name)) {
                        Object instance = createInstance(clazz);
                        singletonInstances.put(name, instance);
                    }
                }
            }
        }


        // Register a class in the container
        public void register(Class<?> clazz) {
            String name = clazz.getSimpleName().toLowerCase();
            // If it implements an interface, use the interface name instead of the class name
            Class<?>[] interfaces = clazz.getInterfaces();

                // Register interface
            if (interfaces.length > 0) {
                for (Class<?> interfaz : interfaces) {
                    registeredClasses.put(interfaz.getSimpleName().toLowerCase(), clazz);
                }
            }
            registeredClasses.put(name, clazz);
            System.out.println("Registered: " + name + " -> " + clazz.getName());  // Debug
        }

        private Object createInstance(Class<?> clazz) {
            try {

                Constructor<?> constructor = getConstructorWithDependencies(clazz);

                Object[] parameters = resolveConstructorParameters(constructor);

                Object instance = constructor.newInstance(parameters);

                injectDependencies(instance);

                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Error creating instance of " + clazz.getName(), e);
            }
        }


        private void injectDependencies(Object object) {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inyect.class)) {
                    Class<?> fieldType = field.getType();
                    String fieldName = fieldType.getSimpleName().toLowerCase();

                    Object instance = singletonInstances.get(fieldName);
                    if (instance == null) {
                        Class<?> clazz = registeredClasses.get(fieldName);
                        if (clazz != null) {
                            instance = createInstance(clazz);
                            singletonInstances.put(fieldName, instance);
                        } else {
                            throw new RuntimeException("No registered instance for dependency: " + fieldName);
                        }
                    }

                    field.setAccessible(true);
                    try {
                        field.set(object, instance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error injecting dependency into field " + field.getName(), e);
                    }
                }
            }
        }


        // Get the constructor with dependencies (the one with parameters)
        private Constructor<?> getConstructorWithDependencies(Class<?> clazz) {
            // Get all public constructors
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            // If there is more than one constructor, select the one with parameters
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() > 0) {
                    return constructor;
                }
            }
            // If there are no constructors with parameters, use the default constructor
            try {
                return clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("No valid constructor found in " + clazz.getName(), e);
            }
        }


        private final Set<String> instancesInProgress = new HashSet<>();

        private Object[] resolveConstructorParameters(Constructor<?> constructor) {
            Parameter[] parameters = constructor.getParameters();
            Object[] resolvedParameters = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Class<?> paramType = parameters[i].getType();
                String paramName = paramType.getSimpleName().toLowerCase();

                if (List.class.isAssignableFrom(paramType)) {
                    Type genericType = parameters[i].getParameterizedType();
                    if (!(genericType instanceof ParameterizedType)) {
                        throw new RuntimeException("Cannot determine generic type for List in " + constructor.getDeclaringClass().getName());
                    }

                    Type actualType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                    if (!(actualType instanceof Class)) {
                        throw new RuntimeException("Unknown generic type for List in " + constructor.getDeclaringClass().getName());
                    }

                    Class<?> listElementType = (Class<?>) actualType;


                    List<Object> list = registeredClasses.values().stream()
                            .filter(clazz -> listElementType.isAssignableFrom(clazz))
                            .map(clazz -> {
                                try {
                                    return createInstance(clazz);
                                } catch (Exception e) {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    resolvedParameters[i] = list;
                }

                else if (paramType.equals(String.class)) {
                    resolvedParameters[i] = "";
                } else if (paramType.equals(int.class) || paramType.equals(Integer.class)) {
                    resolvedParameters[i] = 0;
                } else if (paramType.equals(boolean.class) || paramType.equals(Boolean.class)) {
                    resolvedParameters[i] = false;
                } else if (paramType.equals(double.class) || paramType.equals(Double.class)) {
                    resolvedParameters[i] = 0.0;
                } else if (paramType.equals(float.class) || paramType.equals(Float.class)) {
                    resolvedParameters[i] = 0.0f;
                } else {

                    if (instancesInProgress.contains(paramName)) {
                        throw new RuntimeException("Dependency cycle detected while creating: " + paramName);
                    }

                    instancesInProgress.add(paramName);
                    try {
                        resolvedParameters[i] = getClassInstance(paramName, paramType);
                    } finally {
                        instancesInProgress.remove(paramName);
                    }
                }
            }
            return resolvedParameters;
        }




        public <T> T getClassInstance(String name, Class<T> type) {

            if (singletonInstances.containsKey(name)) {
                return type.cast(singletonInstances.get(name));
            }
            if (registeredClasses.containsKey(name)) {
                Class<?> clazz = registeredClasses.get(name);
                Object instance = createInstance(clazz);
                return type.cast(instance);
            }
            throw new RuntimeException("Instance with name " + name + " not found");
        }


        public <T> T getClassInstance(Class<T> type) {
            return getClassInstance(type.getSimpleName().toLowerCase(), type);
        }


        public Class<?> getClassAnnotation(String name) {
            try {
                Class<?> clazz = nainClass;
                if (clazz.isAnnotationPresent(ClassName.class)) {
                    ClassName classNameAnnotation = clazz.getAnnotation(ClassName.class);
                    String className = classNameAnnotation.value();
                    Class<?> dynamicClass = Class.forName(className);
                    Object instance = dynamicClass.getDeclaredConstructor().newInstance();
                    Method saludoMethod = dynamicClass.getMethod(name);

                    if (saludoMethod == null) {

                    } else {
                        saludoMethod.invoke(instance);
                    }

                } else {
                    System.out.println("Class does not have the ClassName annotation.");
                }
            } catch (ClassNotFoundException e) {
                System.out.println("An error occurred while trying to read the package annotation: " + e.getMessage());
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }
    }

