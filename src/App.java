public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        int name = Integer.parseInt(System.console().readLine("Enter your name: "));
        System.out.println("Hello, " + name + "!");
    }
}
