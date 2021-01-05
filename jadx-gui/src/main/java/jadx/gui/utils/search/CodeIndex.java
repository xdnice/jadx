package jadx.gui.utils.search;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import jadx.api.JavaClass;
import jadx.gui.treemodel.CodeNode;
import jadx.gui.utils.UiUtils;

public class CodeIndex {

	private static final Logger LOG = LoggerFactory.getLogger(CodeIndex.class);

	private final List<CodeNode> values = new ArrayList<>();

	public synchronized void put(CodeNode value) {
		values.add(value);
	}

	public synchronized void removeForCls(JavaClass cls) {
		values.removeIf(v -> v.getJavaNode().getTopParentClass().equals(cls));
	}

	private boolean isMatched(StringRef key, SearchSettings searchSettings) {
		return searchSettings.isMatch(key);
	}

	public Flowable<CodeNode> search(final SearchSettings searchSettings) {
		return Flowable.create(emitter -> {
			LOG.debug("Code search started: {} ...", searchSettings.getSearchString());
			for (CodeNode node : values) {
				if (isMatched(node.getLineStr(), searchSettings)) {
					emitter.onNext(node);
				}
				if (emitter.isCancelled()) {
					LOG.debug("Code search canceled: {}", searchSettings.getSearchString());
					return;
				}
			}
			LOG.debug("Code search complete: {}, memory usage: {}", searchSettings.getSearchString(), UiUtils.memoryInfo());
			emitter.onComplete();
		}, BackpressureStrategy.LATEST);
	}

	public int size() {
		return values.size();
	}
}
