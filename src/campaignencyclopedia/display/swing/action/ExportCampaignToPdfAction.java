package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.pdfbox.exceptions.COSVisitorException;
import plainpdf.Pdf;
import plainpdf.PdfFont;

/**
 * An action to support exporting campaigns out to PDF files.
 * @author adam
 */
public class ExportCampaignToPdfAction extends AbstractExtractToPdfAction {

    private final Frame m_parent;

    private static final Logger LOGGER = Logger.getLogger(ExportCampaignToPdfAction.class.getName());

    public ExportCampaignToPdfAction(Frame parent, CampaignDataManager cdm, String name, boolean includeSecrets) {
        super(parent, cdm, name, includeSecrets);
        m_parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {

        Campaign campaign = m_cdm.getData();
        final Pdf pdf = new Pdf(PdfFont.HELVETICA, NORMAL);
        try {
            pdf.insertBlankLine(TITLE);
            pdf.insertBlankLine(TITLE);
            pdf.insertBlankLine(TITLE);
            pdf.renderLine(campaign.getName(), PdfFont.HELVETICA_BOLD, TITLE);
            if (!m_includeSecrets) {
                pdf.renderLine("without secret data");
            } else {
                pdf.renderLine("includes secret data");
            }

            pdf.insertPageBreak();

            // Get and Sort the Entities of this Campaign
            List<Entity> entities = new ArrayList<>(campaign.getEntities());
            Collections.sort(entities);

            // For each entity
            for (Entity entity : entities) {
                // If the entity is secret and we don't want to export secret data, skip it.
                if (entity.isSecret() && !m_includeSecrets) {
                    continue;
                }
                pdf.renderLine(entity.getName(), PdfFont.HELVETICA_BOLD, SECTION);

                // Entity Type
                if (entity.isSecret()) {
                    pdf.renderLine("Secret " + entity.getType().getDisplayString(), Color.RED);
                } else {
                    pdf.renderLine(entity.getType().getDisplayString());
                }
                pdf.insertBlankLine();

                // Public & Secret Data
                EntityData publicData = entity.getPublicData();
                processEntityData(publicData, pdf, "Public Data");
                if (m_includeSecrets) {
                    EntityData secretData = entity.getSecretData();
                    processEntityData(secretData, pdf, "Secret Data");
                }
                pdf.insertBlankLine();
            }

            // Show Save Dialog
            final JFileChooser chooser = new JFileChooser("./campaigns");
            chooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    String path = file.getAbsolutePath();
                    return path.endsWith(".pdf");
                }

                @Override
                public String getDescription() {
                    return "PDF documents";
                }
            });
            if (chooser.showSaveDialog(m_parent) == JFileChooser.APPROVE_OPTION) {
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            String path = chooser.getSelectedFile().getAbsolutePath();
                            if (!path.endsWith(".pdf")) {
                                path = path + ".pdf";
                            }
                            pdf.saveAs(path);
                        } catch (IOException | COSVisitorException ex) {
                            LOGGER.log(Level.SEVERE, "Error exporting to PDF.", ex);
                        }
                    }
                }).start();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error exporting to PDF.", ex);
        }
    }

}