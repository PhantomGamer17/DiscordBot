package novaz.modules.pathofexile;

import novaz.modules.pathofexile.obj.PoEItem;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemAnalyzer {
	private final Pattern levelRequirementPattern = Pattern.compile("\nLevel: ([0-9]+)");
	private PoEItem item;
	private List<IPoEAnalyzePart> analyzers = new ArrayList<>();
	private List<Boolean> hasAnalyzed = new ArrayList<>();

	private static final String paragraphSplitter = "--------";

	public ItemAnalyzer() {
		loadAnalyzeParts();
	}

	private void loadAnalyzeParts() {
		Reflections reflections = new Reflections("novaz.modules.pathofexile.analyzepart");
		Set<Class<? extends IPoEAnalyzePart>> classes = reflections.getSubTypesOf(IPoEAnalyzePart.class);
		for (Class<? extends IPoEAnalyzePart> clazz : classes) {
			try {
				IPoEAnalyzePart obj = clazz.getConstructor().newInstance();
				analyzers.add(obj);
				hasAnalyzed.add(false);

			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	public PoEItem attemptToANALyze(String toanalyze) {
		item = new PoEItem();
		String[] paragraphs = breakIntoParts(toanalyze);
		int totalAnalyzers = analyzers.size();
		for (String paragraph : paragraphs) {
			paragraph = paragraph.trim();
			boolean paragraphIsAnalyzed = false;
			for (int i = 0; i < totalAnalyzers; i++) {
				if (!hasAnalyzed.get(i)) {
					if (analyzers.get(i).canAnalyze(paragraph)) {
						item = analyzers.get(i).analyze(item, paragraph);
						hasAnalyzed.set(i, true);
						paragraphIsAnalyzed = true;
						break;
					}
				}
			}
			if (!paragraphIsAnalyzed) {
				System.out.println("!!!!!!!!!!!!!!!!");
				System.out.println(paragraph);
				System.out.println("!!!!!!!!!!!!!!!!");
			}
		}
		return item;
	}

	private String[] breakIntoParts(String toanalyze) {
		return toanalyze.split(paragraphSplitter);
	}

	private void analyzeRarity(String toanalyze) {


		// Grab levelPattern

		Matcher matcher = levelRequirementPattern.matcher(toanalyze);
		if (matcher.find()) {
			item.requirementLevel = Integer.parseInt(matcher.group(1));
		}
	}
}
