package util;

import java.util.Scanner;

public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public int lerInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String linha = scanner.nextLine().trim();
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Erro: Digite um numero valido.");
            }
        }
    }

    public double lerDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String linha = scanner.nextLine().trim().replace(",", ".");
                return Double.parseDouble(linha);
            } catch (NumberFormatException e) {
                System.out.println("Erro: Digite um numero valido (use ponto ou virgula).");
            }
        }
    }

    public String lerString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String lerStringObrigatorio(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linha = scanner.nextLine().trim();
            if (!linha.isEmpty()) {
                return linha;
            }
            System.out.println("Erro: Este campo e obrigatorio.");
        }
    }

    public int lerIntOpcao(String prompt) {
        try {
            String linha = scanner.nextLine().trim();
            if (linha.isEmpty()) return -2;
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
