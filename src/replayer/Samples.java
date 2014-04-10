package replayer;

import java.util.ArrayList;
import java.util.List;

public class Samples {
	
	
	static List<String> sample1() {
		List<String> result = new ArrayList<String>();
		result.add("bipush 10");
		result.add("istore_1");
		result.add("iload_1");
		result.add("iconst_2");
		result.add("imul");
		result.add("istore_1");
		result.add("return");
		return result;
	}
	
	static List<String> sample2() {
		List<String> result = new ArrayList<String>();
		result.add("iconst_1");
		result.add("newarray int");
		result.add("dup");
		result.add("iconst_0");
		result.add("bipush	10");
		result.add("iastore");
		result.add("astore_1");
		result.add("aload_1");
		result.add("iconst_0");
		result.add("aload_1");
		result.add("iconst_0");
		result.add("iaload");
		result.add("iconst_2");
		result.add("imul");
		result.add("iastore");
		result.add("return");
		return result;
	}
	
	static List<String> sample3() {
		List<String> result = new ArrayList<String>();
		result.add("NEW     #2; //class Foo$Bar");
		result.add("dup");
		result.add("iconst_5");
		result.add("invokespecial   #3; //Method Foo$Bar.\"<init>\":(I)V");
		
		result.add("aload_0");
		result.add("invokespecial	#1; //Method java/lang/Object.\"<init>\":()");
		result.add("return");

		result.add("astore_1");
		result.add("aload_1");
		result.add("bipush  6");
		result.add("putfield        #4; //Field Foo$Bar.x:I");
		result.add("return");
		return result;
	}

}
