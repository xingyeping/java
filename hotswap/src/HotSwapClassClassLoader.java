public class HotSwapClassClassLoader extends ClassLoader {
    public HotSwapClassClassLoader() {
        super(HotSwapClassClassLoader.class.getClassLoader());
    }
    public Class loadByte(byte[] classByte) {
        return defineClass(null, classByte, 0,classByte.length);
    }
}
