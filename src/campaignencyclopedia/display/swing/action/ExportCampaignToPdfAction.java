package campaignencyclopedia.display.swing.action;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Month;
import campaignencyclopedia.data.TimelineEntry;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    /** A Logger. */
    private static final Logger LOGGER = Logger.getLogger(ExportCampaignToPdfAction.class.getName());

    /**
     * Creates an action for exporting the PDF action.
     * @param parent The parent window for positioning dialogs launched by this action.
     * @param cdm a campaign data manager to get the data from.
     * @param name the name of the action to display.
     * @param includeSecrets truer if secrets should be included.
     */
    public ExportCampaignToPdfAction(Frame parent, CampaignDataManager cdm, String name, boolean includeSecrets) {
        super(parent, cdm, name, includeSecrets);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent ae) {
        Campaign campaign = m_cdm.getData();
        final Pdf pdf = new Pdf(PdfFont.HELVETICA, NORMAL);

        try {
            // == TITLE PAGE ==========
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


            // == TIMELINE ============
            exportTimeline(pdf);
            
            // == TABLE OF CONTENTS ===
            // Get and Sort the Entities of this Campaign - these will also be used when we process each Entity.
            List<Entity> entities = new ArrayList<>(campaign.getEntities());
            Collections.sort(entities);
            pdf.renderLine("Table of Contents", PdfFont.HELVETICA_BOLD, SECTION);
            pdf.insertBlankLine(SECTION);
            for (Entity entity : entities) {
                if (entity.isSecret()) {
                    if (m_includeSecrets) {
                        pdf.renderLine(entity.getName(), SECRET_COLOR);
                    }
                } else {
                    pdf.renderLine(entity.getName());
                }
            }


            // == ENTITY DATA =========
            // For each entity
            for (Entity entity : entities) {
                // If the entity is secret and we don't want to export secret data, skip it.
                if (entity.isSecret() && !m_includeSecrets) {
                    continue;
                }
                pdf.insertPageBreak();
                // Entity Type
                if (entity.isSecret()) {
                    pdf.renderLine(entity.getName(), PdfFont.HELVETICA_BOLD, SECTION, SECRET_COLOR);
                    pdf.renderLine("Secret " + entity.getType().getDisplayString(), SECRET_COLOR);
                } else {
                    pdf.renderLine(entity.getName(), PdfFont.HELVETICA_BOLD, SECTION);
                    pdf.renderLine(entity.getType().getDisplayString());
                }
                pdf.insertBlankLine();

                // Public & Secret Data
                EntityData publicData = entity.getPublicData();
                processEntityData(publicData, campaign.getRelationships(entity.getId()), pdf, false);
                if (m_includeSecrets) {
                    EntityData secretData = entity.getSecretData();
                    processEntityData(secretData, campaign.getRelationships(entity.getId()), pdf, true);
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

    /**
     * Exports the timeline of the campaign.
     * @param pdf PDF file to render the timeline data to.
     * @throws IOException
     */
    private void exportTimeline(Pdf pdf) throws IOException {
        pdf.renderLine("Campaign Timeline", PdfFont.HELVETICA_BOLD, SECTION);
        pdf.insertBlankLine(SECTION);
        Map<TimelineDate, List<TimelineEntry>> timeline = new HashMap<>();
        for (TimelineEntry tle : m_cdm.getTimelineData()) {
            // If secret and secrets not permitted, skip it.
            if (tle.isSecret() && !m_includeSecrets) {
                continue;
            }
            TimelineDate tld = new TimelineDate(tle.getMonth(), tle.getYear());
            if (timeline.get(tld) == null) {
                timeline.put(tld, new ArrayList<TimelineEntry>());
            }
            timeline.get(tld).add(tle);
        }
        List<TimelineDate> timelineDates = new ArrayList<>(timeline.keySet());
        Collections.sort(timelineDates);
        for (TimelineDate date : timelineDates) {
            pdf.renderLine(date.month.getName() + " " + date.year, PdfFont.HELVETICA_BOLD);
            List<TimelineEntry> entries = timeline.get(date);
            Collections.sort(entries);
            for (TimelineEntry tle : entries) {
                String msg;
                if (tle.getTitle() == null || tle.getTitle().isEmpty()) {
                    msg = m_cdm.getEntity(tle.getAssociatedId()).getName();
                } else {
                    msg = tle.getTitle();
                }
                if (tle.isSecret()) {
                    msg += " (secret)";
                    pdf.renderLine(msg, SECRET_COLOR);
                } else {
                    pdf.renderLine(msg);
                }
            }
            pdf.insertBlankLine(6);
        }
        pdf.insertPageBreak();
    }

    /** A Helper class for organizing timeline data for PDF rendering. */
    private class TimelineDate implements Comparable<TimelineDate> {
        private final Month month;
        private final int year;

        private TimelineDate(Month m, int y) {
            month = m;
            year = y;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + Objects.hashCode(this.month);
            hash = 67 * hash + this.year;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TimelineDate other = (TimelineDate) obj;
            if (!Objects.equals(this.month, other.month)) {
                return false;
            }
            if (this.year != other.year) {
                return false;
            }
            return true;
        }

        /** {@Override} */
        @Override
        public int compareTo(TimelineDate t) {
            // Compare by Year
            if (year > t.year) {
                return 1;
            } else if (year < t.year) {
                return -1;
            }
            return (month.compareTo(t.month));
        }
    }
}