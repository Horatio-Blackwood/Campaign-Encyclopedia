package campaignencyclopedia.data;

import java.util.ArrayList;
import java.util.List;

/**
 * The types of Relationships that Entities can have with one another.
 * @author adam
 */
public enum RelationshipType implements Displayable {
    ADVENTURES_WITH("Adventures with"),
    ASSOCIATED_WITH("Associated with"),
    BROTHER_OF("Brother of"),
    CARRIES("Carries"),
    CARRIED_BY("Carried by"),
    CAUSED("Caused"),
    CAUSED_BY("Caused By"),
    CONTAINS("Contains"),
    CONTROLS("Controls"),
    CORRUPTED("Corrupted"),
    CORRUPTED_BY("Corrupted by"),
    CREATED("Created"),
    CREATED_BY("Created by"),
    DAUGHTER_OF("Daughter of"),
    DEFEATED("Defeated"),
    DEFEATED_BY("Defeated by"),
    DESIRES("Desires"),
    DESTROYED("Destroyed"),
    DESTROYED_BY("Destroyed by"),
    DESTROYED_DURING("Destroyed during"),
    EMPLOYS("Employs"),
    ENEMY_OF("Enemy of"),
    FATHER_OF("Father of"),
    FEARS("Fears"),
    FOLLOWED_BY("Followed by"),
    FRIENDS_WITH("Friends with"),
    FROM("From"),
    HAS("Has"),
    HATES("Hates"),
    HIDES("Hides"),
    IN("In"),
    IS("Is"),
    KILLED("Killed"),
    KILLED_BY("Killed by"),
    MOTHER_OF("Mother of"),
    LIVES_IN("Lives in"),
    LOVES("Loves"),
    MARRIED_TO("Married to"),
    MEMBER_OF("Member of"),
    NEAR("Near"),
    ON("On"),
    OPPOSES("Opposes"),
    OPPOSED_BY("Opposed by"),
    OWED_DEBT_BY("Owed a debt by"),
    OWES_DEBT_TO("Owes a debt to"),
    OWNS("Owns"),
    OWNED_BY("Owned by"),
    POSSESSES("Possesses"),
    POSSESSED_BY("Possessed by"),
    PRECEDED_BY("Preceded by"),
    PROTECTS("Protects"),
    RESPECTS("Respects"),
    RELATED_TO("Related to"),
    RULES("Rules"),
    SAVED("Saved"),
    SAVED_BY("Saved by"),
    SEE_ALSO("See also"),
    SEEKS("Seeks"),
    SEEKS_REVENGE_ON("Seeks revenge on"),
    SERVANT_OF("Servant of"),
    SISTER_OF("Sister of"),
    SON_OF("Son of"),
    SOUGHT_BY("Sought by"),
    STOLE("Stole"),
    STOLEN_BY("Stolen by"),
    STOLEN_FROM("Stolen from"),
    SWORN_TO("Sworn to"),
    THINKS_HIGHLY_OF("Thinks highly of"),
    THINKS_POORLY_OF("Thinks poorly of"),
    UNDER("Under"),
    WORKS_FOR("Works for"),
    WORKS_WITH("Works with"),
    WORSHIPS("Worships"),
    WORSHIPPED_BY("Worshipped by");

    /** The display string for this RelationshipType. */
    private final String m_displayString;

    /**
     * Creates a RelationshipType
     * @param userString the user display string.
     */
    private RelationshipType(String userString) {
        m_displayString = userString;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayString() {
        return m_displayString;
    }

    public static List<String> getStringList() {
        List<String> stringList = new ArrayList<>();
        for (RelationshipType rt : values()) {
            stringList.add(rt.getDisplayString());
        }
        return stringList;
    }
}