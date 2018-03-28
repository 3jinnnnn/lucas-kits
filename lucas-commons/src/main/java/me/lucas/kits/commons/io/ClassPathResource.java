/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.lucas.kits.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import me.lucas.kits.commons.utils.Assert;
import me.lucas.kits.commons.utils.ClassUtils;
import me.lucas.kits.commons.utils.ObjectUtils;
import me.lucas.kits.commons.utils.ResourceUtils;
import me.lucas.kits.commons.utils.StringUtils;

/**
 * {@link Resource} implementation for class path resources.
 * Uses either a given ClassLoader or a given Class for loading resources.
 *
 * <p>Supports resolution as <code>java.io.File</code> if the class path
 * resource resides in the file system, but not for resources in a JAR.
 * Always supports resolution as URL.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see ClassLoader#getResourceAsStream(String)
 * @see Class#getResourceAsStream(String)
 */
public class ClassPathResource extends AbstractResource {

    private final String path;

    private ClassLoader classLoader;

    private Class<?> clazz;

    /**
     * Create a new ClassPathResource for ClassLoader usage.
     * A leading slash will be removed, as the ClassLoader
     * resource access methods will not accept it.
     * <p>The thread context class loader will be used for
     * loading the resource.
     * @param path the absolute path within the class path
     * @see ClassLoader#getResourceAsStream(String)
     */
    public ClassPathResource(String path) {
        this(path, (ClassLoader) null);
    }

    /**
     * Create a new ClassPathResource for ClassLoader usage.
     * A leading slash will be removed, as the ClassLoader
     * resource access methods will not accept it.
     * @param path the absolute path within the classpath
     * @param classLoader the class loader to load the resource with,
     * or <code>null</code> for the thread context class loader
     * @see ClassLoader#getResourceAsStream(String)
     */
    public ClassPathResource(String path, ClassLoader classLoader) {
        Assert.notNull(path, "Path must not be null");
        String pathToUse = StringUtils.cleanPath(path);
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        this.path = pathToUse;
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    /**
     * Create a new ClassPathResource for Class usage.
     * The path can be relative to the given class,
     * or absolute within the classpath via a leading slash.
     * @param path relative or absolute path within the class path
     * @param clazz the class to load resources with
     * @see Class#getResourceAsStream
     */
    public ClassPathResource(String path, Class<?> clazz) {
        Assert.notNull(path, "Path must not be null");
        this.path = StringUtils.cleanPath(path);
        this.clazz = clazz;
    }

    /**
     * Create a new ClassPathResource with optional ClassLoader and Class.
     * Only for internal usage.
     * @param path relative or absolute path within the classpath
     * @param classLoader the class loader to load the resource with, if any
     * @param clazz the class to load resources with, if any
     */
    protected ClassPathResource(String path, ClassLoader classLoader, Class<?> clazz) {
        this.path = StringUtils.cleanPath(path);
        this.classLoader = classLoader;
        this.clazz = clazz;
    }

    /**
     * @return the path for this resource (as resource path within the class path).
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * @return the ClassLoader that this resource will be obtained from.
     */
    public final ClassLoader getClassLoader() {
        return (this.classLoader != null ? this.classLoader : this.clazz.getClassLoader());
    }

    /**
     * @return This implementation opens an InputStream for the given class path resource.
     * @see ClassLoader#getResourceAsStream(String)
     * @see Class#getResourceAsStream(String)
     */
    public InputStream getInputStream() throws IOException {
        InputStream is = null;
        if (this.clazz != null) {
            is = this.clazz.getResourceAsStream(this.path);
        } else {
            is = this.classLoader.getResourceAsStream(this.path);
        }
        if (is == null) {
            throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
        }
        return is;
    }

    /**
     * @return This implementation returns a URL for the underlying class path resource.
     * @see ClassLoader#getResource(String)
     * @see Class#getResource(String)
     */
    public URL getURL() throws IOException {
        URL url = null;
        if (this.clazz != null) {
            url = this.clazz.getResource(this.path);
        } else {
            url = this.classLoader.getResource(this.path);
        }
        if (url == null) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
        }
        return url;
    }

    /**
     * @return This implementation returns a File reference for the underlying class path
     * resource, provided that it refers to a file in the file system.
     */
    public File getFile() throws IOException {
        return ResourceUtils.getFile(getURL(), getDescription());
    }

    /**
     * @reutnr This implementation determines the underlying File
     * (or jar file, in case of a resource in a jar/zip).
     */
    protected File getFileForLastModifiedCheck() throws IOException {
        URL url = getURL();
        if (ResourceUtils.isJarURL(url)) {
            URL actualUrl = ResourceUtils.extractJarFileURL(url);
            return ResourceUtils.getFile(actualUrl);
        } else {
            return ResourceUtils.getFile(url, getDescription());
        }
    }

    /**
     * @return This implementation creates a ClassPathResource, applying the given path
     * relative to the path of the underlying resource of this descriptor.
     */
    public Resource createRelative(String relativePath) {
        String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return new ClassPathResource(pathToUse, this.classLoader, this.clazz);
    }

    /**
     * @return This implementation returns the name of the file that this class path
     * resource refers to.
     */
    public String getFilename() {
        return StringUtils.getFilename(this.path);
    }

    /**
     * @return This implementation returns a description that includes the class path location.
     */
    public String getDescription() {
        return "class path resource [" + this.path + ']';
    }

    /**
     * @return This implementation compares the underlying class path locations.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ClassPathResource) {
            ClassPathResource otherRes = (ClassPathResource) obj;
            return (this.path.equals(otherRes.path) && ObjectUtils.nullSafeEquals(this.classLoader, otherRes.classLoader)
                    && ObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz));
        }
        return false;
    }

    /**
     * @return This implementation returns the hash code of the underlying
     * class path location.
     */
    public int hashCode() {
        return this.path.hashCode();
    }

}
