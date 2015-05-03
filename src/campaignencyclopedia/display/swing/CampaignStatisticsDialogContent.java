package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipManager;
import campaignencyclopedia.data.TimelineEntry;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.swing.JLabel;
import javax.swing.JPanel;
import toolbox.display.EditListener;
import toolbox.display.dialog.DialogContent;

/**
 * A dialog content for showing the Campaign Statistics data.
 * @author adam
 */
public class CampaignStatisticsDialogContent implements DialogContent {

    /** The main content pane of this dialog. */
    private JPanel m_content;
    private final CampaignDataManager m_cdm;
    private final CampaignStatistics m_stats;

    /**
     * Constructor.
     * @param cdm a data manager for a campaign, must not be null.
     * @throws IllegalArgumentException if cdm is null.
     */
    public CampaignStatisticsDialogContent(CampaignDataManager cdm) {
        if (cdm == null) {
            throw new IllegalArgumentException("Parameter 'cdm' cannot be null.");
        }
        m_cdm = cdm;
        m_stats = processData();
        initialize();

    }

    /** {@inheritDoc} */
    @Override
    public Component getContent() {
        return m_content;
    }

    /** {@inheritDoc} */
    @Override
    public void setDialogEditListener(EditListener el) {
        // ignored
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDataCommittable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCommitPermitted() {
        return true;
    }

    private JLabel buildLabel(String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(JLabel.RIGHT);
        return label;
    }

    private JLabel buildValueLabel(Object value) {
        JLabel label = new JLabel(String.valueOf(value));
        label.setHorizontalAlignment(JLabel.LEFT);
        return label;
    }

    private void initialize() {
        m_content = new JPanel(new GridBagLayout());

        // Row Zero - Entities
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        m_content.add(buildLabel("Entity Count:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.entities), gbc);

        // Row One - Secret Entities
        gbc.gridx = 0;
        gbc.gridy = 1;
        m_content.add(buildLabel("Secret Entities:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.secretEntities), gbc);

        // Row Two - PC Entities
        gbc.gridx = 0;
        gbc.gridy = 2;
        m_content.add(buildLabel("PCs:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.pcs), gbc);

        // Row Three - NPC Entities
        gbc.gridx = 0;
        gbc.gridy = 3;
        m_content.add(buildLabel("NPCs:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.npcs), gbc);

        // Row Four - Place Entities
        gbc.gridx = 0;
        gbc.gridy = 4;
        m_content.add(buildLabel("Places:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.places), gbc);

        // Row Five - Event Entities
        gbc.gridx = 0;
        gbc.gridy = 5;
        m_content.add(buildLabel("Events:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.events), gbc);

        // Row Six - Event Entities
        gbc.gridx = 0;
        gbc.gridy = 6;
        m_content.add(buildLabel("Organizations:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.organizations), gbc);

        // Row Seven - Event Entities
        gbc.gridx = 0;
        gbc.gridy = 7;
        m_content.add(buildLabel("Items:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.items), gbc);

        // Row Eight - Description Words
        gbc.gridx = 0;
        gbc.gridy = 8;
        m_content.add(buildLabel("Total Descriptive Words:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.descriptionWords), gbc);

        // Row Nine - Secret Description Words
        gbc.gridx = 0;
        gbc.gridy = 9;
        m_content.add(buildLabel("Secret Descriptive Words:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.secretDescriptionWords), gbc);

        // Row Ten - Description Words / Entity
        gbc.gridx = 0;
        gbc.gridy = 10;
        m_content.add(buildLabel("Descriptive Words / Entity:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.getDescriptionWordsPerEntity()), gbc);

        // Row 11 - Secret Description Words / Entity
        gbc.gridx = 0;
        gbc.gridy = 11;
        m_content.add(buildLabel("Secret Descriptive Words / Entity:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.getSecretDescriptionWordsPerEntity()), gbc);

        // Row 12 - Tags
        gbc.gridx = 0;
        gbc.gridy = 12;
        m_content.add(buildLabel("Total Tags:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.tags), gbc);

        // Row 13 - Secret Tags
        gbc.gridx = 0;
        gbc.gridy = 13;
        m_content.add(buildLabel("Secret Tags:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.secretTags), gbc);

        // Row 14 - Tags / Entity
        gbc.gridx = 0;
        gbc.gridy = 14;
        m_content.add(buildLabel("Total Tags / Entity:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.getTagsPerEntity()), gbc);

        // Row 15 - Tags / Entity
        gbc.gridx = 0;
        gbc.gridy = 15;
        m_content.add(buildLabel("Secret Tags / Entity:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.getSecretTagsPerEntity()), gbc);

        // Row 16 - Relationships
        gbc.gridx = 0;
        gbc.gridy = 16;
        m_content.add(buildLabel("Total Relationships:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.relationships), gbc);

        // Row 17 - Secret Relationships
        gbc.gridx = 0;
        gbc.gridy = 17;
        m_content.add(buildLabel("Secret Relationships:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.secretRelationships), gbc);

        // Row 18 - Timeline Entries
        gbc.gridx = 0;
        gbc.gridy = 18;
        m_content.add(buildLabel("Timeline Entries:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.timelineEntries), gbc);

        // Row 19 - Timeline Entries Linking to Secret Entities
        gbc.gridx = 0;
        gbc.gridy = 19;
        m_content.add(buildLabel("Secret Timeline Entries:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.secretTimelineEntries), gbc);

        // Row 20 - Configured Months
        gbc.gridx = 0;
        gbc.gridy = 20;
        m_content.add(buildLabel("Months in Calendar:"), gbc);

        gbc.gridx = 1;
        m_content.add(buildValueLabel(m_stats.configuredMonths), gbc);
    }

    private CampaignStatistics processData() {
        CampaignStatistics cs = new CampaignStatistics();
        Campaign campaign = m_cdm.getData();

        for (Entity entity : campaign.getEntities()) {
            // Entities
            cs.entities += 1;
            if (entity.isSecret()) {
                cs.secretEntities += 1;
            }

            // Entity Types
            switch(entity.getType()) {
                case EVENT:
                    cs.events += 1;
                    break;
                case ITEM:
                    cs.items += 1;
                    break;
                case NON_PLAYER_CHARACTER:
                    cs.npcs += 1;
                    break;
                case ORGANIZATION:
                    cs.organizations += 1;
                    break;
                case PLACE:
                    cs.places += 1;
                    break;
                case PLAYER_CHARACTER:
                    cs.pcs += 1;
                    break;
            }

            // Description
            String description = entity.getPublicData().getDescription().trim();
            String secretDescription = entity.getSecretData().getDescription().trim();
            if (!secretDescription.isEmpty()) {
                cs.secretDescriptionWords += secretDescription.split(" ").length;
                cs.descriptionWords += secretDescription.split(" ").length;
            }
            if (!description.isEmpty()) {
                cs.descriptionWords += description.split(" ").length;
            }


            // Tags
            int secretTags = entity.getSecretData().getTags().size();
            cs.secretTags += secretTags;
            cs.tags += (entity.getPublicData().getTags().size() + secretTags);
        }

        // Relationships
        for (RelationshipManager relationshipManager : campaign.getAllRelationships().values()) {
            for (Relationship rel : relationshipManager.getAllRelationships()) {
                if (rel.isSecret()) {
                    cs.secretRelationships++;
                }
                cs.relationships++;
            }
        }

        // Timeline Entries
        Set<TimelineEntry> timelineEntries = campaign.getTimelineEntries();
        cs.timelineEntries = timelineEntries.size();
        for (TimelineEntry tle : timelineEntries) {
            if (tle.isSecret()) {
                cs.secretTimelineEntries += 1;
            }
        }

        // Campaign Calendar Configuration
        cs.configuredMonths = campaign.getCalendar().getMonths().size();

        return cs;
    }

    private class CampaignStatistics {
        private int entities = 0;
        private int secretEntities = 0;

        private int events = 0;
        private int pcs = 0;
        private int npcs = 0;
        private int places = 0;
        private int organizations = 0;
        private int items = 0;

        private int descriptionWords = 0;
        private int secretDescriptionWords = 0;

        private int tags = 0;
        private int secretTags = 0;

        private int relationships = 0;
        private int secretRelationships = 0;

        private int timelineEntries = 0;
        private int secretTimelineEntries = 0;

        private int configuredMonths = 0;

        /** Decimal format for displaying fractional values. */
        private final DecimalFormat df = new DecimalFormat("0.00");

        private String getDescriptionWordsPerEntity() {
            if (entities != 0) {
                return String.valueOf(df.format(descriptionWords / (1.0d * entities)));
            }
            return "N/A";
        }

        private String getSecretDescriptionWordsPerEntity() {
            if (entities != 0) {
                return String.valueOf(df.format(secretDescriptionWords / (1.0d * entities)));
            }
            return "N/A";
        }

        private String getTagsPerEntity() {
            if (entities != 0) {
                return String.valueOf(df.format(tags / (1.0d * entities)));
            }
            return "N/A";
        }

        private String getSecretTagsPerEntity() {
            if (entities != 0) {
                return String.valueOf(df.format(secretTags / (1.0d * entities)));
            }
            return "N/A";
        }
    }
}