package cc.leevi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jiang on 2017-03-26.
 */
public class MediaProcess {
    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("d:\\convert_commands.txt")));
        String line = null;
        while ((line = br.readLine())!=null){
            long cur = System.currentTimeMillis();
            Process process = Runtime.getRuntime().exec(line);
            System.out.println(String.format("开始转换：%s",line));
            process.waitFor();
            System.out.println(String.format("转换完成，耗时：%d",(System.currentTimeMillis()-cur)));
        }
        System.out.println("全部转换完成");
        br.close();
    }
}
