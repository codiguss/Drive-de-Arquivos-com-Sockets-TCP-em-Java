import java.io.*;
import java.net.*;

public class Servidor {
    static String[] usuarios = {"gustavo", "carlos"};
    static String[] senhas = {"gustavo1", "carlos1"};

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Servidor iniciado na porta 12345");

            Socket clienteSocket = serverSocket.accept();
            System.out.println("Cliente conectado!");

            DataInputStream entrada = new DataInputStream(clienteSocket.getInputStream());
            DataOutputStream saida = new DataOutputStream(clienteSocket.getOutputStream());

            saida.writeUTF("Digite o usuário:");
            String usuario = entrada.readUTF();
            saida.writeUTF("Digite a senha:");
            String senha = entrada.readUTF();

            if (autenticar(usuario, senha)) {
                saida.writeUTF("Login realizado com sucesso!");
                prepararPastas(usuario);
            } else {
                saida.writeUTF("Usuário ou senha incorretos. Encerrando conexão.");
                clienteSocket.close();
                return;
            }

            while (true) {
                saida.writeUTF("Escolha uma opção: LISTAR, UPLOAD, DOWNLOAD, SAIR");
                String opcao = entrada.readUTF();

                if (opcao.equalsIgnoreCase("LISTAR")) {
                    listarArquivos(usuario, saida);
                } else if (opcao.equalsIgnoreCase("UPLOAD")) {
                    receberArquivo(usuario, entrada, saida);
                } else if (opcao.equalsIgnoreCase("DOWNLOAD")) {
                    enviarArquivo(usuario, entrada, saida);
                } else if (opcao.equalsIgnoreCase("SAIR")) {
                    saida.writeUTF("Conexão encerrada.");
                    break;
                } else {
                    saida.writeUTF("Opção inválida.");
                }
            }

            clienteSocket.close();
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean autenticar(String usuario, String senha) {
        for (int i = 0; i < usuarios.length; i++) {
            if (usuarios[i].equals(usuario) && senhas[i].equals(senha)) {
                return true;
            }
        }
        return false;
    }

    static void prepararPastas(String usuario) {
        new File("armazenamento/" + usuario + "/pdf").mkdirs();
        new File("armazenamento/" + usuario + "/jpg").mkdirs();
        new File("armazenamento/" + usuario + "/txt").mkdirs();
    }

    static void listarArquivos(String usuario, DataOutputStream saida) throws IOException {
        File pastaUsuario = new File("armazenamento/" + usuario);
        StringBuilder lista = new StringBuilder();

        for (File tipo : pastaUsuario.listFiles()) {
            lista.append(tipo.getName()).append(":\n");
            for (File arquivo : tipo.listFiles()) {
                lista.append(" - ").append(arquivo.getName()).append("\n");
            }
        }

        saida.writeUTF(lista.toString());
    }

    static void receberArquivo(String usuario, DataInputStream entrada, DataOutputStream saida) throws IOException {
        saida.writeUTF("Digite o tipo do arquivo (pdf, jpg ou txt):");
        String tipo = entrada.readUTF();
        saida.writeUTF("Digite o nome do arquivo:");
        String nome = entrada.readUTF();
        saida.writeUTF("Pronto para receber o arquivo.");

        File arquivo = new File("armazenamento/" + usuario + "/" + tipo + "/" + nome);
        FileOutputStream fos = new FileOutputStream(arquivo);

        long tamanho = entrada.readLong();
        byte[] buffer = new byte[4096];
        int bytes;

        while (tamanho > 0 && (bytes = entrada.read(buffer, 0, (int)Math.min(buffer.length, tamanho))) != -1) {
            fos.write(buffer, 0, bytes);
            tamanho -= bytes;
        }
        fos.close();
        saida.writeUTF("Upload concluído!");
    }

    static void enviarArquivo(String usuario, DataInputStream entrada, DataOutputStream saida) throws IOException {
        saida.writeUTF("Digite o caminho do arquivo para download (ex: pdf/arquivo.pdf):");
        String caminho = entrada.readUTF();
        File arquivo = new File("armazenamento/" + usuario + "/" + caminho);

        if (arquivo.exists()) {
            saida.writeLong(arquivo.length());
            FileInputStream fis = new FileInputStream(arquivo);

            byte[] buffer = new byte[4096];
            int bytes;
            while ((bytes = fis.read(buffer)) != -1) {
                saida.write(buffer, 0, bytes);
            }
            fis.close();
            saida.writeUTF("Download concluído!");
        } else {
            saida.writeLong(-1);
            saida.writeUTF("Arquivo não encontrado.");
        }
    }
}