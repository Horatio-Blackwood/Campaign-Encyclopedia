package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import static campaignencyclopedia.display.swing.action.AbstractExtractToPdfAction.TITLE;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.pdfbox.exceptions.COSVisitorException;
import plainpdf.Pdf;
import plainpdf.PdfFont;

/**
 *
 * @author adam
 */
public class ExportEntityToPdf extends AbstractExtractToPdfAction {

    /** The Entity. */
    private final Entity m_entity;

    private static final Logger LOGGER = Logger.getLogger(ExportCampaignToPdfAction.class.getName());

    public ExportEntityToPdf(Frame parent, Entity entity, CampaignDataManager cdm, String name, boolean includeSecrets) {
        super(parent, cdm, name, includeSecrets);
        m_entity = entity;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        try {
            final Pdf pdf = new Pdf();
            pdf.renderLine(m_entity.getName(), PdfFont.HELVETICA_BOLD, TITLE);

            // Entity Type
            if (m_entity.isSecret()) {
                pdf.renderLine("Secret " + m_entity.getType().getDisplayString(), Color.RED);
            } else {
                pdf.renderLine(m_entity.getType().getDisplayString());
            }
            pdf.insertBlankLine();

            // Public & Secret Data
            EntityData publicData = m_entity.getPublicData();
            processEntityData(publicData, m_cdm.getRelationshipsForEntity(m_entity.getId()), pdf, false);
            if (m_includeSecrets) {
                EntityData secretData = m_entity.getSecretData();
                processEntityData(secretData, m_cdm.getRelationshipsForEntity(m_entity.getId()), pdf, true);
            }
            pdf.insertBlankLine();

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
