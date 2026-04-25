package com.halo.lims.dto.report;

public class BrandingDTO {
    private String headerImage;
    private String footerImage;
    private int headerMarginMm;
    private int footerMarginMm;
    private int headerHeightMm;
    private int footerHeightMm;
    private int pageMarginTopMm;
    private int pageMarginBottomMm;

    public BrandingDTO() {}

    public BrandingDTO(String headerImage, String footerImage, int headerMarginMm, int footerMarginMm, int headerHeightMm, int footerHeightMm, int pageMarginTopMm, int pageMarginBottomMm) {
        this.headerImage = headerImage;
        this.footerImage = footerImage;
        this.headerMarginMm = headerMarginMm;
        this.footerMarginMm = footerMarginMm;
        this.headerHeightMm = headerHeightMm;
        this.footerHeightMm = footerHeightMm;
        this.pageMarginTopMm = pageMarginTopMm;
        this.pageMarginBottomMm = pageMarginBottomMm;
    }

    public static BrandingDTOBuilder builder() {
        return new BrandingDTOBuilder();
    }

    // Getters and Setters
    public String getHeaderImage() { return headerImage; }
    public void setHeaderImage(String headerImage) { this.headerImage = headerImage; }

    public String getFooterImage() { return footerImage; }
    public void setFooterImage(String footerImage) { this.footerImage = footerImage; }

    public int getHeaderMarginMm() { return headerMarginMm; }
    public void setHeaderMarginMm(int headerMarginMm) { this.headerMarginMm = headerMarginMm; }

    public int getFooterMarginMm() { return footerMarginMm; }
    public void setFooterMarginMm(int footerMarginMm) { this.footerMarginMm = footerMarginMm; }

    public int getHeaderHeightMm() { return headerHeightMm; }
    public void setHeaderHeightMm(int headerHeightMm) { this.headerHeightMm = headerHeightMm; }

    public int getFooterHeightMm() { return footerHeightMm; }
    public void setFooterHeightMm(int footerHeightMm) { this.footerHeightMm = footerHeightMm; }

    public int getPageMarginTopMm() { return pageMarginTopMm; }
    public void setPageMarginTopMm(int pageMarginTopMm) { this.pageMarginTopMm = pageMarginTopMm; }

    public int getPageMarginBottomMm() { return pageMarginBottomMm; }
    public void setPageMarginBottomMm(int pageMarginBottomMm) { this.pageMarginBottomMm = pageMarginBottomMm; }

    public static class BrandingDTOBuilder {
        private String headerImage;
        private String footerImage;
        private int headerMarginMm;
        private int footerMarginMm;
        private int headerHeightMm;
        private int footerHeightMm;
        private int pageMarginTopMm;
        private int pageMarginBottomMm;

        public BrandingDTOBuilder headerImage(String headerImage) { this.headerImage = headerImage; return this; }
        public BrandingDTOBuilder footerImage(String footerImage) { this.footerImage = footerImage; return this; }
        public BrandingDTOBuilder headerMarginMm(int headerMarginMm) { this.headerMarginMm = headerMarginMm; return this; }
        public BrandingDTOBuilder footerMarginMm(int footerMarginMm) { this.footerMarginMm = footerMarginMm; return this; }
        public BrandingDTOBuilder headerHeightMm(int headerHeightMm) { this.headerHeightMm = headerHeightMm; return this; }
        public BrandingDTOBuilder footerHeightMm(int footerHeightMm) { this.footerHeightMm = footerHeightMm; return this; }
        public BrandingDTOBuilder pageMarginTopMm(int pageMarginTopMm) { this.pageMarginTopMm = pageMarginTopMm; return this; }
        public BrandingDTOBuilder pageMarginBottomMm(int pageMarginBottomMm) { this.pageMarginBottomMm = pageMarginBottomMm; return this; }

        public BrandingDTO build() {
            return new BrandingDTO(headerImage, footerImage, headerMarginMm, footerMarginMm, headerHeightMm, footerHeightMm, pageMarginTopMm, pageMarginBottomMm);
        }
    }
}