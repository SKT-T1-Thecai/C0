package miniplc0java.analysizer;

import java.util.ArrayList;

public class Instruction {
    String instruction_name;
    byte instruction_byte;
    // 是否要操作数
    boolean with_operands;
    ArrayList<Byte> instruction_num = new ArrayList<>();
    public Instruction(String instruction_name,ArrayList<Byte> instruction_num)
    {
        this.instruction_name = instruction_name;
        this.instruction_byte = get_instruction_byte_by_name(instruction_name);
        this.instruction_num = instruction_num;
        this.with_operands = true;
    }
    public Instruction(String instruction_name)
    {
        this.instruction_name = instruction_name;
        this.instruction_byte = get_instruction_byte_by_name(instruction_name);
        this.instruction_num = new ArrayList<>();
        this.with_operands = false;
    }
    public static byte get_instruction_byte_by_name(String name)
    {
        switch (name)
        {
            case "nop":
                return 0x00;
            case "push":
                return 0x01;
            case "pop":
                return 0x02;
            case "popn":
                return 0x03;
            case "dup":
                return 0x04;
            case "loca":
                return 0x0a;
            case "arga":
                return 0x0b;
            case "globa":
                return 0x0c;
            case "load.8":
                return 0x10;
            case "load.16":
                return 0x11;
            case "load.32":
                return 0x12;
            case "load.64":
                return 0x13;
            case "store.8":
                return 0x14;
            case "store.16":
                return 0x15;
            case "store.32":
                return 0x16;
            case "store.64":
                return 0x17;
            case "alloc":
                return 0x18;
            case "free":
                return 0x19;
            case "stackalloc":
                return 0x1a;
            case "add.i":
                return 0x20;
            case "sub.i":
                return 0x21;
            case "mul.i":
                return 0x22;
            case "div.i":
                return 0x23;
            case "add.f":
                return 0x24;
            case "sub.f":
                return 0x25;
            case "mul.f":
                return 0x26;
            case "div.f":
                return 0x27;
            case "div.u":
                return 0x28;
            case "shl":
                return 0x29;
            case "shr":
                return 0x2a;
            case "and":
                return 0x2b;
            case "or":
                return 0x2c;
            case "xor":
                return 0x2d;
            case "not":
                return 0x2e;
            case "cmp.i":
                return 0x30;
            case "cmp.u":
                return 0x31;
            case "cmp.f":
                return 0x32;
            case "neg.i":
                return 0x34;
            case "neg.f":
                return 0x35;
            case "itof":
                return 0x36;
            case "foti":
                return 0x37;
            case "shrl":
                return 0x38;
            case "set.lt":
                return 0x39;
            case "set.gt":
                return 0x3a;
            case "br":
                return 0x41;
            case "br.false":
                return 0x42;
            case "br.true":
                return 0x43;
            case "call":
                return 0x48;
            case "ret":
                return 0x49;
            case "callname":
                return 0x4a;
            case "scan.i":
                return 0x50;
            case "scan.c":
                return 0x51;
            case "scan.f":
                return 0x52;
            case "print.i":
                return 0x54;
            case "print.c":
                return 0x55;
            case "print.f":
                return 0x56;
            case "print.s":
                return 0x57;
            case "println":
                return 0x58;
            case "panic":
                return (byte) 0xfe;
            case "null":
                return (byte)0xff;
            default:throw new Error("illegal instruction string.");
        }
    }
    public static long get_num_by_byte_array(ArrayList<Byte> bytes)
    {
        long ans = 0;
        for(int i=0;i<bytes.size();i++)
        {
            ans = ans*256;
            ans += ((long)bytes.get(i)+256)%256;
        }
        return ans;
    }
    public static ArrayList<Byte> get_byte_array_by_int(int a)
    {
        ArrayList<Byte> bytes = new ArrayList<>();
        int[] res =new int[4];
        for(int i=0;i<4;i++)
        {
            res[3-i] = a%256;
            a/=256;
        }
        for(int i=0;i<=3;i++)
            bytes.add((byte)res[i]);
        return bytes;
    }
    public static ArrayList<Byte> get_byte_array_by_long(long a)
    {
        ArrayList<Byte> bytes = new ArrayList<>();
        long[] res =new long[8];
        for(int i=0;i<8;i++)
        {
            res[7-i] = a%256;
            a/=256;
        }
        for(int i=0;i<=7;i++)
            bytes.add((byte)res[i]);
        return bytes;
    }

    public static void main(String[] args) {
        ArrayList<Byte> bytes = get_byte_array_by_long((long)256*256*256*127*256);
        for(int i=0;i<bytes.size();i++)
        {
            System.out.println(((int)(bytes.get(i))+256)%256);
        }

    }



}
