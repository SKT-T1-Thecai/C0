package miniplc0java.tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import miniplc0java.util.*;
public class ReadFile {
    public ArrayList<String> lineBuffer = new ArrayList<>();
    public String InputSrc;
    public String NowStr;
    public Pos NowPos;
    public Pos NextPos;
    public Pos LastPos;
    public Pos GetNextPos()
    {
        if(this.NowPos.row==lineBuffer.size()-1&&this.NowPos.col==NowStr.length()-1)
            return null;
        else if(this.NowPos.row<lineBuffer.size()-1&&this.NowPos.col==NowStr.length()-1)
            return NowPos.nextRow();
        else return  NowPos.nextPos();
    }
    public Pos GetNextNextPos()
    {
        Pos nextPos = GetNextPos();
        if(nextPos==null)return null;
        String nextStr = lineBuffer.get(nextPos.row);
        if(nextPos.row==lineBuffer.size()-1&&nextPos.col==nextStr.length()-1)
            return null;
        else if(nextPos.row<lineBuffer.size()-1&&nextPos.col==nextStr.length()-1)
            return nextPos.nextRow();
        else return  nextPos.nextPos();

    }

    public char GetNextChar()
    {
        if(GetNextPos()==null) return 0;
        else return this.lineBuffer.get(this.GetNextPos().row).charAt(this.GetNextPos().col);
    }
    public char GetNextNextChar()
    {
        Pos NextNextPos = GetNextNextPos();
        if(NextNextPos!=null)
        {
            return lineBuffer.get(NextNextPos.row).charAt(NextNextPos.col);
        }else return 0;
    }
    public char GetNowChar()
    {
        return this.NowStr.charAt(this.NowPos.col);
    }
    public void GoNext()
    {
        this.LastPos = NowPos;
        this.NowPos=GetNextPos();
        this.NextPos=GetNextPos();
        if(this.NowPos.row==this.LastPos.row+1)
            this.NowStr = lineBuffer.get(NowPos.row);
    }
    public ReadFile(String inputSrc)
    {
        this.InputSrc = inputSrc;
    }
    public void ReadInit()
    {
        this.ReadAll();
        this.NowPos = new Pos(0,0);
        this.NowStr = lineBuffer.get(0);
        this.NextPos = GetNextPos();

    }
    public void ReadAll()
    {
       // String InputSrc = "D:\\IDEA projects\\C0\\in.txt";
        File file = new File(InputSrc);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                lineBuffer.add(tempStr+"\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }
    public void PrintAll()
    {
        for(int i=0;i<lineBuffer.size();i++)
        {
            System.out.print(lineBuffer.get(i));
        }
    }
    public boolean isEOF()
    {
        return this.NowPos.row==lineBuffer.size()-1&&this.NowPos.col==NowStr.length()-1;
    }
    public static void main(String[] args) {
        ReadFile readFile= new ReadFile("D:\\IDEA projects\\C0\\in.txt");

        readFile.ReadInit();
        while(!readFile.isEOF())
        {
            System.out.print(readFile.GetNextNextChar());
            //System.out.print(readFile.GetNowChar());
            readFile.GoNext();

        }


    }
}
