package ustc.krazy.manager;

import lombok.Data;
import ustc.krazy.structure.bFrame;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ustc.krazy.constant.BufferConstants.FRAME_SIZE;
import static ustc.krazy.constant.BufferConstants.MAX_PAGES;

/**
 * @author krazy
 * @date 2025/1/6
 */
@Data
public class DataStorageManager {
    private RandomAccessFile currFile;
    private int numPages;
    private int[] pages;
    private long threadId = Thread.currentThread().getId();

    public static Map<Long, Integer> ICounter;
    public static Map<Long, Integer> OCounter;

    public DataStorageManager() {
        this.numPages = 0;
        this.pages = new int[MAX_PAGES];
        currFile = null;
        for(int i = 0;i < MAX_PAGES;i++) {
            this.pages[i] = 0;
        }
        ICounter = new HashMap<>();
        OCounter = new HashMap<>();
    }

    public int openFile(String fileName){
        try{
            this.currFile = new RandomAccessFile(fileName,"rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
    public int closeFile() {
        try {
            if(currFile != null) {
                this.currFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        this.currFile = null;
        return 1;
    }
    public bFrame readPage(int pageId) throws IOException{
        byte[] buffer = new byte[FRAME_SIZE];
        try {
            long pos = (long) pageId * FRAME_SIZE;
            currFile.seek(pos);
            int length = currFile.read(buffer, 0, FRAME_SIZE);
            if(length == 0) {
                System.out.println("未读取到任何内容");
            } else if (length == -1) {
                System.out.println("文件已读取到末尾");
            }
            ICounter.merge(threadId,1, Integer::sum);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读异常");
        }
        return new bFrame(buffer);
    }
    public bFrame writePage(int frameId,bFrame frame){
        try {
            this.currFile.seek((long) frameId * FRAME_SIZE);
            this.currFile.write(Arrays.toString(frame.filed).getBytes(), 0, FRAME_SIZE);
            OCounter.merge(threadId,1, Integer::sum);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件写异常");
        }
        return frame;
    }
    public RandomAccessFile getFile() {
        return this.currFile;
    }
    public void incNumPages() {
        this.numPages++;
    }
    public void setUse(int index,int useBit) {
        this.pages[index] = useBit;
    }
    public int getUse(int index) {
        return this.pages[index];
    }

    public int getIOCounter(long thread) {
        return ICounter.getOrDefault(thread,0);
    }

    public int getOCounter(long thread) {
        return OCounter.getOrDefault(thread,0);
    }

}
