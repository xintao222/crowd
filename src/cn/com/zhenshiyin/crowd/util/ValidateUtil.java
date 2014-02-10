package cn.com.zhenshiyin.crowd.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateUtil {
	 //wi=2(n-1)(mod11)
	 static int[] wi={7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2,1};

	 //verifydigit 
	 static int[]vi={1,0,10,9,8,7,6,5,4,3,2};

	 static int[] ai=new int[18];

	 /**验证是否是正确的邮箱格式
		 * @param email
		 * @return	true表示是正确的邮箱格式,false表示不是正确邮箱格式
		 */
		public static boolean isEmail(String email){		
			// 1、\\w+表示@之前至少要输入一个匹配字母或数字或下划线、点、中横线
			String regular = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
			Pattern pattern = Pattern.compile(regular);
			boolean flag = false;
			if( email != null ){			
				Matcher matcher = pattern.matcher(email);	
				flag = matcher.matches();		
			}		
			return flag;
		}	
		
		public static boolean isPassword(String password){
			String regular = "^[A-Za-z0-9]$";
			Pattern pattern = Pattern.compile(regular);
			boolean flag = false;
			if(StringUtil.isNotEmpty(password)){			
				Matcher matcher = pattern.matcher(password);	
				flag = matcher.matches();		
			}	
			return flag;
		}
		
		/**验证是否是手机号格式
		 * 该方法还不是很严谨,只是可以简单验证
		 * @param mobile
		 * @return	true表示是正确的手机号格式,false表示不是正确的手机号格式
		 */
		public static boolean isMobile(String mobile){
			//当前运营商号段分配
			//中国移动号段 1340-1348 135 136 137 138 139 150 151 152 157 158 159 187 188 147
			//中国联通号段 130 131 132 155 156 185 186 145
			//中国电信号段 133 1349 153 180 189
			String regular = "1[3,4,5,8]{1}\\d{9}";
			Pattern pattern = Pattern.compile(regular);	
			boolean flag = false;
			if( mobile != null ){			
				Matcher matcher = pattern.matcher(mobile);
				flag = matcher.matches();			
			}
			return flag;
		}
		
		/**验证是否是整数
		 * @param str
		 */
		public static boolean isInteger(String str){
			   boolean flag = false ;
			   Pattern pattern = Pattern.compile("\\d*$");
			   //字符串不为空;
			   if ( str.length() > 0 ) {   
			    Matcher matcher = pattern.matcher(str);
			    if ( matcher.matches() == true ) {
			     flag = true ;
			     //除去以0开头的情况;
			     if ( str.length() > 1 ) {
			      if ( ( str.charAt(0) == '0' ) ){
			       flag = false ;
			      }
			     }
			    }
			   }
			   return flag ;
			}

	 
		/**
		 * 严格验证身份证号的方法，15位、18位均可
		 * @param idcard
		 */
	 public static boolean Verify(String idcard)
	 {
	  if(idcard.length()==15){
	   idcard=uptoeighteen(idcard);
	  }
	  if(idcard.length()!=18){
	   return false;
	  }
	  String verify=idcard.substring(17,18);
	  if(verify.equalsIgnoreCase(getVerify(idcard))){
	   return true;
	  }
	  return false;
	 }
	 //getverify
	 private static String getVerify(String eightcardid){
	  int remaining=0;
	  if(eightcardid.length()==18){
	   eightcardid=eightcardid.substring(0,17);
	  }
	  if(eightcardid.length()==17){
	   int sum=0;
	   for(int i=0;i<17;i++){
	    String k=eightcardid.substring(i,i+1);
	    try{
	    	ai[i]=Integer.parseInt(k);
	    }catch (Exception e) {
			// TODO: handle exception
		}
	    
	   }
	   for(int i=0;i<17;i++){
	    sum=sum+wi[i]*ai[i];
	   }
	   remaining=sum%11;
	  }

	  return remaining==2?"X":String.valueOf(vi[remaining]);
	 }
	 //15updateto18
	 private static String uptoeighteen(String fifteencardid){
	  String eightcardid=fifteencardid.substring(0,6);
	  eightcardid=eightcardid+"19";
	  eightcardid=eightcardid+fifteencardid.substring(6,15);
	  eightcardid=eightcardid+getVerify(eightcardid);
	  return eightcardid;
	 }


	 public static boolean valiFlightNo(String no){
		 if(no!=null){
				String isFlightNo="^([A-Za-z0-9]{2}\\d{1,4})|\\d{3,4}$";
				Pattern  p = Pattern.compile(isFlightNo);
				return p.matcher(no).find();
			}
		 return false;
	 }
	 

	public static boolean valiID(String id){
		if(id!=null){
			String isIDCard="^(\\d{15}$|^\\d{18}$|^\\d{17}(\\d|X|x))$";
			Pattern  p = Pattern.compile(isIDCard);
			return p.matcher(id).find();
		}
		return false;
	}
	
	public static boolean valiDigit(String id){
		if(id!=null){
			String isIDCard="^\\d+$";
			Pattern  p = Pattern.compile(isIDCard);
			return p.matcher(id).find();
		}
		return false;
	}
	
	public static boolean valiName(String name){
		if(name!=null){
			if(isEngishName(name)){
				return true;
			}else if(isChineseName(name)){
				return true;
			}
		}
		return false;
		
	}
	
	private static boolean isEngishName(String name) {
		if(name!=null){
			String isIDCard="^[a-zA-Z]+[/]{1}[a-zA-Z]+$";
			Pattern  p = Pattern.compile(isIDCard);
			return p.matcher(name).find();
		}
		 return false;
	}
	
	private static boolean isChineseName(String name) {
		
		for(int i=0; i<name.length();i++){
			boolean b = isChinese(name.charAt(i));
			if(!b) return false;
		}
        return true;
    }
	
	public static boolean isChinese(char ch){
		 Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
	        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
	             || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
	            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
	            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
	            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
	            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
	            return true;
	        }
	        return false;
	}
}
