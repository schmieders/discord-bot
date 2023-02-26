package dev.schmieders;

public class App {

    public static void main(String[] args) {
        try {
            new Bot();
        } catch (InterruptedException e) {
            System.err.println("The bot could not be started.");
        }
    }
}
