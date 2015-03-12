package org.unesco.jisis.dbserver.classloader;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

class ByteArray
    implements java.io.Serializable
{
    public ByteArray(byte[] bytes)
    {
        m_bytes = bytes;
    }
    
    public byte[] getBytes()
    {
        return m_bytes;
    }
    
    private byte[] m_bytes;
}    


/**
 * HashtableClassLoader
 */
public class HashtableClassLoader extends java.lang.ClassLoader
    implements IClassLoaderStrategy
{
    /**
     *
     */
    public HashtableClassLoader()
    {
        this(HashtableClassLoader.class.getClassLoader());
    }
    /**
     *
     */
    public HashtableClassLoader(Map table)
    {
        this(HashtableClassLoader.class.getClassLoader(), table);
    }
    /**
     *
     */
    public HashtableClassLoader(ClassLoader parent)
    {
        this(parent, new HashMap());
    }
    /**
     *
     */
    public HashtableClassLoader(ClassLoader parent, Map table)
    {
        super(parent);

        m_classtable = table;
    }
    

    /**
     *
     */
    public void putClass(String className, byte[] bytes)
    {
        m_classtable.put(className, new ByteArray(bytes));
    }
    
    
    /**
     * Return byte array (which will be turned into a Class instance
     * via ClassLoader.defineClass) for class
     */
    public byte[] findClassBytes(String className)
    {
        try
        {
            ByteArray byteArray = (ByteArray)m_classtable.get(className);
            byte[] bytes = byteArray.getBytes();
            return bytes;
        }
        catch (Exception ex)
        {
            return null;
        }
    }
    
    /**
     * Return URL for resource given by resourceName
     */
    public URL findResourceURL(String resourceName)
    {
        return null;
    }
    /**
     * Return Enumeration of resources corresponding to
     * resourceName.
     */
    public Enumeration findResourcesEnum(String resourceName)
    {
        return null;
    }
    
    /**
     * Return full path to native library given by the name
     * libraryName.
     */
    public String findLibraryPath(String libraryName)
    {
        return null;
    }
    
    
    /**
     *
     */
    public Class findClass(String className)
        throws ClassNotFoundException
    {
        byte[] bytes = findClassBytes(className);
        if (bytes==null)
        {
            throw new ClassNotFoundException();
        }

        return defineClass(className, bytes, 0, bytes.length);
    }

    // Internal members
    //
    private Map m_classtable;
    
    
    // Driver
    //
    public static void main(String[] args)
        throws Exception
    {
        // Try the HashtableClassLoader
        HashtableClassLoader hcl = new HashtableClassLoader();

        // Load "Hello.class" from root dir into the Hashtable
        FileInputStream fis = new FileInputStream("/Hello.class");
        int ct = fis.available();
        byte[] Hello_bytes = new byte[ct];
        fis.read(Hello_bytes);
        
        hcl.putClass("Hello", Hello_bytes);

        // Try the loadClass
        Object obj = hcl.loadClass("Hello").newInstance();
    }
}
