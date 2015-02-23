package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Relationship;
import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import plainpdf.Pdf;
import plainpdf.PdfFont;

/**
 *
 * @author adam
 */
public abstract class AbstractExtractToPdfAction extends AbstractAction {

    protected static final int TITLE = 38;
    protected static final int SECTION = 24;
    protected static final int SUB_SECTION = 14;
    protected static final int NORMAL = 12;
    protected final CampaignDataManager m_cdm;
    protected final Frame m_parent;
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
     * @param pdf the PDF to render the data into.
     * @throws IOException if an error occurs generating the PDF.
     */
    protected void processEntityData(EntityData data, Pdf pdf, String header) throws IOException {
        String description = data.getDescription().trim();
        Set<Relationship> rels = data.getRelationships();
        if (description.isEmpty() && rels.isEmpty()) {
            return;
        }
        // Render header
        pdf.renderLine(header, PdfFont.HELVETICA_BOLD, SUB_SECTION);

        // Render description, if one exists
        if (!description.isEmpty()) {
            pdf.renderLine("Description", PdfFont.HELVETICA_BOLD, NORMAL);
            String[] paragraphs = description.split("\n");
            for (String par : paragraphs) {
                pdf.renderLine(par);
            }
        }

        // Render relationships, if any exists
        pdf.insertBlankLine();
        if (!data.getRelationships().isEmpty()) {
            List<Relationship> relationships = new ArrayList<>(rels);
            Collections.sort(relationships);
            pdf.renderLine("Relationships", PdfFont.HELVETICA_BOLD, NORMAL);
            for (Relationship rel : relationships) {
                Entity linkedTo = m_cdm.getEntity(rel.getIdOfRelation());
                if (linkedTo.isSecret() && !m_includeSecrets) {
                    // Don't export secret data in this case.
                } else {
                    pdf.renderLine(rel.getType().getDisplayString() + " " + linkedTo.getName());
                }

            }
        }
        pdf.insertBlankLine();
    }
}
