import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            while (true) {
                String mensagem = entrada.readUTF();
                System.out.println(mensagem);

                if (mensagem.contains("Conexão encerrada.") || mensagem.contains("Usuário ou senha incorretos")) {
                    break;
                }

                String resposta = scanner.nextLine();
                saida.writeUTF(resposta);

                if (mensagem.contains("Pronto para receber o arquivo.")) {
                    File arquivo = new File(resposta);
                    saida.writeLong(arquivo.length());
                    FileInputStream fis = new FileInputStream(arquivo);

                    byte[] buffer = new byte[4096];
                    int bytes;
                    while ((bytes = fis.read(buffer)) != -1) {
                        saida.write(buffer, 0, bytes);
                    }
                    fis.close();
                } else if (mensagem.contains("Digite o caminho do arquivo para download")) {
                    String caminho = resposta;
                    long tamanho = entrada.readLong();

                    if (tamanho > 0) {
                        File novoArquivo = new File("downloads_" + caminho.replace("/", "_"));
                        novoArquivo.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(novoArquivo);

                        byte[] buffer = new byte[4096];
                        int bytes;
                        while (tamanho > 0 && (bytes = entrada.read(buffer, 0, (int)Math.min(buffer.length, tamanho))) != -1) {
                            fos.write(buffer, 0, bytes);
                            tamanho -= bytes;
                        }
                        fos.close();
                        System.out.println("Arquivo salvo como: " + novoArquivo.getAbsolutePath());
                    } else {
                        System.out.println(entrada.readUTF()); // Mensagem de erro
                    }
                }
            }
            socket.close();
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}