package miniplc0java.util;

public class Caculate { // 15.16E-4
    public static double ScienceCoutToDouble(StringBuilder integralPart,StringBuilder fractionalPart,StringBuilder exponentPart)
    {int integral = Integer.parseInt(integralPart.toString());
     double fraction = fractionToDouble(fractionalPart);
     int exponent = Integer.parseInt(exponentPart.toString());
     double ans=integral + fraction;
     return ans*Math.pow(10,exponent);
    }
    public static double fractionToDouble(StringBuilder s)//005 -->0.005
    {
        int len = s.length();
        double num= (double)(Integer.parseInt(s.toString()));
        while(len-->0)
        {
            num/=10;
        }
        return num;
    }

    public static void main(String[] args) {
        StringBuilder a= new StringBuilder("15");
        StringBuilder b= new StringBuilder("15");
        StringBuilder c= new StringBuilder("-10");

        System.out.println(ScienceCoutToDouble(a,b,c));
    }
}
