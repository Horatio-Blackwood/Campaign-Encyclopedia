package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipManager;
import java.awt.Color;
import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import plainpdf.Pdf;
import plainpdf.PdfFont;

/**
 * An action to extract data to PDF.
 * @author adam
 */
public abstract class AbstractExtractToPdfAction extends AbstractAction {

    /** Title font size. */
    protected static final int TITLE = 38;

    /** The section header (Entity Name) font size. */
    protected static final int SECTION = 24;

    /** Entity subsection header font size.. */
    protected static final int SUB_SECTION = 14;

    /** The paragraph body font size. */
    protected static final int NORMAL = 12;

    /** The font size for fine print. */
    protected static final int FINE_PRINT = 10;

    /** The Color used to render secret data / headers. */
    protected static final Color SECRET_COLOR = Color.RED;

    /** A campaign data manager to fetch the data to export from. */
    protected final CampaignDataManager m_cdm;

    /** The parent window for positioning dialogs launched by this action. */
    protected final Frame m_parent;

    /** True if secrets should be included in this export. */
    protected final boolean m_includeSecrets;

    public AbstractExtractToPdfAction(Frame parent, CampaignDataManager cdm, String name, boolean includeSecrets) {
        super(name);
        if (cdm == null) {
            throw new IllegalArgumentException("cdm can't be null.");
        }
        m_cdm = cdm;
        m_includeSecrets = includeSecrets;
        m_parent = parent;
    }

    /**
     * Renders the supplied EntityData to the supplied PDF document.
     * @param data the data to render.
     * @param relManager a RelationshipManager for the Entity this EntityData is from.
     * @param pdf the PDF to render the data into.
     * @param secret true if this is secret data.
     * @throws IOException if an error occurs generating the PDF.
     */
    protected void processEntityData(EntityData data, RelationshipManager relManager, Pdf pdf, boolean secret) throws IOException {
        String description = data.getDescription().trim();
        // If not secret and no public data exists, return
        if (!secret && description.isEmpty() && relManager.getPublicRelationships().isEmpty()) {
            return;
        }
        // If secret, and no secret relatinships exist, return
        if (secret && description.isEmpty() && relManager.getSecretRelationships().isEmpty()) {
            return;
        }

        // Render header
        if (secret) {
            pdf.renderLine("Secret Data", PdfFont.HELVETICA_BOLD, SUB_SECTION, SECRET_COLOR);
        } else {
            pdf.renderLine("Public Data", PdfFont.HELVETICA_BOLD, SUB_SECTION);
        }


        // Render description, if one exists
        if (!description.isEmpty()) {
            if (secret) {
                pdf.renderLine("Secret Description", PdfFont.HELVETICA_BOLD, NORMAL, SECRET_COLOR);
            } else {
                pdf.renderLine("Description", PdfFont.HELVETICA_BOLD, NORMAL);
            }
            String[] paragraphs = description.split("\n");
            for (String par : paragraphs) {
                pdf.renderLine(par);
            }
            pdf.insertBlankLine();
        }

        // Render relationships, if any exist
        List<Relationship> relationships = new ArrayList<>();
        if (secret) {
            if (!relManager.getSecretRelationships().isEmpty()) {
                pdf.renderLine("Secret Relationships", PdfFont.HELVETICA_BOLD, NORMAL, SECRET_COLOR);
                relationships.addAll(relManager.getSecretRelationships());
                Collections.sort(relationships);
            }
        } else {
            if (!relManager.getPublicRelationships().isEmpty()) {
                pdf.renderLine("Relationships", PdfFont.HELVETICA_BOLD, NORMAL);
                relationships.addAll(relManager.getPublicRelationships());
                Collections.sort(relationships);
            }
        }
        for (Relationship rel : relationships) {
            Entity linkedTo = m_cdm.getEntity(rel.getRelatedEntity());
            if (linkedTo.isSecret() && !m_includeSecrets) {
                // Don't export secret data in this case.
            } else {
                pdf.renderLine(rel.getRelationshipText() + " " + linkedTo.getName());
            }
        }
        pdf.insertBlankLine();
    }
}