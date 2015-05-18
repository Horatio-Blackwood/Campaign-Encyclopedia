package campaignencyclopedia.data.persistence;

import campaignencyclopedia.data.Month;
import toolbox.file.persistence.json.JsonObject;

/**
 *
 * @author adam
 */
public class MonthTranslator {

    private static final String NAME = "name";
    private static final String INDEX = "index";

    public static JsonObject toJson(Month month) {
        JsonObject json = new JsonObject();
        json.put(NAME, month.getName());
        json.put(INDEX, month.getIndex());
        return json;
    }

    public static Month fromJson(JsonObject json) {
        String name = json.getString(NAME);
        int index = json.getInt(INDEX);
        return new Month(name, index);
    }
}