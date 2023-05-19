import panels.Screen;

public class Runner {
    public static boolean isMulticasting = false;

    public static void main(String[] args) {
        Screen screen = new Screen(args.length > 0 && args[0].equals("debug"));

        screen.start();
    }
}
