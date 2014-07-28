package replayer;
import java.util.Set;
import java.util.HashSet;

public class MyObject {
	private Object obj;
	private Set<String> features;

	public MyObject(Object val){
		this.obj = val;
		this.features = new HashSet<String>();
	}

	public MyObject(Object val, Set<String> features){
		this.obj = val;
		this.features = features;
	}

	public Object getObject(){
		return this.obj;
	}

	public Set<String> getSet(){
		return this.features;
	}

	public void setObject(Object obj){
		this.obj = obj;
	}

	public void setFeatures(HashSet<String> features){
		this.features = features;
	}
}