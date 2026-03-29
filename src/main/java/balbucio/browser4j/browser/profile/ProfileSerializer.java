package balbucio.browser4j.browser.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles serialization and deserialization of ProfileEntry to/from a profile.json file
 * located inside the profile directory.
 */
public class ProfileSerializer {

    private static final String PROFILE_FILE = "profile.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /** Saves a ProfileEntry to &lt;profileDir&gt;/profile.json */
    public static void save(ProfileEntry entry) throws IOException {
        Path dir = entry.getProfileDir();
        Files.createDirectories(dir);
        Path file = dir.resolve(PROFILE_FILE);
        Files.writeString(file, GSON.toJson(new ProfileEntryDto(entry)));
    }

    /** Reads a ProfileEntry from &lt;profileDir&gt;/profile.json, or null if not found. */
    public static ProfileEntry load(Path profileDir) throws IOException {
        Path file = profileDir.resolve(PROFILE_FILE);
        if (!Files.exists(file)) return null;
        String json = Files.readString(file);
        ProfileEntryDto dto = GSON.fromJson(json, ProfileEntryDto.class);
        return dto.toEntry();
    }

    // ---- Internal DTO (flat, Gson-friendly) ----

    private static class ProfileEntryDto {
        String profileId;
        String displayName;
        String profilePath;
        String theme;
        String language;
        String timezone;
        double zoomLevel;
        java.util.Map<String, Object> customFlags;

        ProfileEntryDto(ProfileEntry e) {
            ProfilePreferences p = e.getPreferences();
            this.profileId   = e.getProfileId();
            this.displayName = e.getDisplayName();
            this.profilePath = e.getProfilePath();
            this.theme       = p.getTheme().getValue();
            this.language    = p.getLanguage();
            this.timezone    = p.getTimezone();
            this.zoomLevel   = p.getZoomLevel();
            this.customFlags = p.getCustomFlags();
        }

        ProfileEntry toEntry() {
            ProfilePreferences.Theme themeEnum = java.util.Arrays.stream(ProfilePreferences.Theme.values())
                    .filter(t -> t.getValue().equalsIgnoreCase(theme))
                    .findFirst()
                    .orElse(ProfilePreferences.Theme.SYSTEM);

            ProfilePreferences.Builder prefsBuilder = ProfilePreferences.builder()
                    .theme(themeEnum)
                    .language(language)
                    .timezone(timezone)
                    .zoomLevel(zoomLevel);

            if (customFlags != null) {
                customFlags.forEach(prefsBuilder::flag);
            }

            return new ProfileEntry(profileId, displayName, profilePath, prefsBuilder.build());
        }
    }
}
