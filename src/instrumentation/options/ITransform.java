package instrumentation.options;

import org.objectweb.asm.tree.ClassNode;

public interface ITransform {
  void transform(ClassNode cn);
}
