package tools;

import java.util.ArrayList;

public class String_id {
	/*code prit sur http://codes-sources.commentcamarche.net/source/view/53122/1251490#browser*/
	
	public static String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabdefghijklmnopqrstuvwxyz";
	
	
	public static String incrementer(String s) {
		char[] chars = s.toCharArray();
	    for (int i = chars.length - 1; i >= 0; i--) {
	      if (chars[i] < caracteres.charAt(caracteres.length()-1)) {
	        chars[i]++;
	        return String.valueOf(chars);
	      }
	      chars[i] = caracteres.charAt(0);
	    }
	    return String.valueOf(chars);
	  }
	
	/**
     * Fonction qui incremenre une chaine de caracteres ou
     * qui retourne null si la chaine de carateres a atteint
     * le maximum
     * @param s
     *     la chaine a incrementer
     * @return la chaine incrementee ou null
     *
     * @see AttaqueBruteForce#nextChar
     */
    /*public static String incrementer(String s){
        String res=null;
        if(s!=null){
            if(s.length()==0){
                res=caracteres.charAt(caracteres.length()-1)+"";
            }
            else{
                boolean temp = true;
                for(int u=0; u<s.length(); u++){
                    if(s.charAt(u)!=caracteres.charAt(caracteres.length()-1)){
                        temp=false;
                        break;
                    }
                }
                if(!temp){
                    char dernier = s.charAt(s.length()-1);
                    if(dernier==caracteres.charAt(caracteres.length()-1)){
                        res=incrementer(s.substring(0, s.length()-1))+caracteres.charAt(0);
                    }
                    else{
                        res=s.substring(0, s.length()-1)+nextChar(dernier);
                    }
                }
                else{
                    res=null;
                }
            }
        }
        return res;
    }*/

    /**
     * Fonction qui permet de retourner le caractere suivant
     * @param car
     *     le caractere a incrementer
     * @return le caractere suivant
     */
    public static char nextChar(char car){
        int i=0;
        for(int j=0; j<caracteres.length(); j++){
            i++;
            if(caracteres.charAt(j)==car){
                break;
            }
        }
        return caracteres.charAt(i);
    }

}
