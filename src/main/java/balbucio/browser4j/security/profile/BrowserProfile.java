package balbucio.browser4j.security.profile;

public class BrowserProfile {
    private final String profileId;
    private final FingerprintProfile fingerprint;

    private BrowserProfile(Builder builder) {
        this.profileId = builder.profileId;
        this.fingerprint = builder.fingerprint;
    }

    public String getProfileId() { return profileId; }
    public FingerprintProfile getFingerprint() { return fingerprint; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String profileId = java.util.UUID.randomUUID().toString();
        private FingerprintProfile fingerprint = FingerprintProfile.builder().build();

        public Builder profileId(String profileId) {
            this.profileId = profileId;
            return this;
        }

        public Builder fingerprint(FingerprintProfile fingerprint) {
            this.fingerprint = fingerprint;
            return this;
        }

        public BrowserProfile build() {
            return new BrowserProfile(this);
        }
    }
}
