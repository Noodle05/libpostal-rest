package org.gaofamily.libpostal.server.options;

import java.io.Serializable;

/**
 * @author Wei Gao
 * @since 8/12/16
 */
public class ExpanderOptionsJson implements Serializable {
    private static final long serialVersionUID = 1242238488006235996L;


    private String[] languages;
    private Short addressComponents;
    private Boolean latinAscii;
    private Boolean transliterate;
    private Boolean stripAccents;
    private Boolean decompose;
    private Boolean lowercase;
    private Boolean trimString;
    private Boolean dropParentheticals;
    private Boolean replaceNumericHyphens;
    private Boolean deleteNumericHyphens;
    private Boolean splitAlphaFromNumeric;
    private Boolean replaceWordHyphens;
    private Boolean deleteWordHyphens;
    private Boolean deleteFinalPeriods;
    private Boolean deleteAcronymPeriods;
    private Boolean dropEnglishPossessives;
    private Boolean deleteApostrophes;
    private Boolean expandNumex;
    private Boolean romanNumerals;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public Short getAddressComponents() {
        return addressComponents;
    }

    public void setAddressComponents(Short addressComponents) {
        this.addressComponents = addressComponents;
    }

    public Boolean getLatinAscii() {
        return latinAscii;
    }

    public void setLatinAscii(Boolean latinAscii) {
        this.latinAscii = latinAscii;
    }

    public Boolean getTransliterate() {
        return transliterate;
    }

    public void setTransliterate(Boolean transliterate) {
        this.transliterate = transliterate;
    }

    public Boolean getStripAccents() {
        return stripAccents;
    }

    public void setStripAccents(Boolean stripAccents) {
        this.stripAccents = stripAccents;
    }

    public Boolean getDecompose() {
        return decompose;
    }

    public void setDecompose(Boolean decompose) {
        this.decompose = decompose;
    }

    public Boolean getLowercase() {
        return lowercase;
    }

    public void setLowercase(Boolean lowercase) {
        this.lowercase = lowercase;
    }

    public Boolean getTrimString() {
        return trimString;
    }

    public void setTrimString(Boolean trimString) {
        this.trimString = trimString;
    }

    public Boolean getDropParentheticals() {
        return dropParentheticals;
    }

    public void setDropParentheticals(Boolean dropParentheticals) {
        this.dropParentheticals = dropParentheticals;
    }

    public Boolean getReplaceNumericHyphens() {
        return replaceNumericHyphens;
    }

    public void setReplaceNumericHyphens(Boolean replaceNumericHyphens) {
        this.replaceNumericHyphens = replaceNumericHyphens;
    }

    public Boolean getDeleteNumericHyphens() {
        return deleteNumericHyphens;
    }

    public void setDeleteNumericHyphens(Boolean deleteNumericHyphens) {
        this.deleteNumericHyphens = deleteNumericHyphens;
    }

    public Boolean getSplitAlphaFromNumeric() {
        return splitAlphaFromNumeric;
    }

    public void setSplitAlphaFromNumeric(Boolean splitAlphaFromNumeric) {
        this.splitAlphaFromNumeric = splitAlphaFromNumeric;
    }

    public Boolean getReplaceWordHyphens() {
        return replaceWordHyphens;
    }

    public void setReplaceWordHyphens(Boolean replaceWordHyphens) {
        this.replaceWordHyphens = replaceWordHyphens;
    }

    public Boolean getDeleteWordHyphens() {
        return deleteWordHyphens;
    }

    public void setDeleteWordHyphens(Boolean deleteWordHyphens) {
        this.deleteWordHyphens = deleteWordHyphens;
    }

    public Boolean getDeleteFinalPeriods() {
        return deleteFinalPeriods;
    }

    public void setDeleteFinalPeriods(Boolean deleteFinalPeriods) {
        this.deleteFinalPeriods = deleteFinalPeriods;
    }

    public Boolean getDeleteAcronymPeriods() {
        return deleteAcronymPeriods;
    }

    public void setDeleteAcronymPeriods(Boolean deleteAcronymPeriods) {
        this.deleteAcronymPeriods = deleteAcronymPeriods;
    }

    public Boolean getDropEnglishPossessives() {
        return dropEnglishPossessives;
    }

    public void setDropEnglishPossessives(Boolean dropEnglishPossessives) {
        this.dropEnglishPossessives = dropEnglishPossessives;
    }

    public Boolean getDeleteApostrophes() {
        return deleteApostrophes;
    }

    public void setDeleteApostrophes(Boolean deleteApostrophes) {
        this.deleteApostrophes = deleteApostrophes;
    }

    public Boolean getExpandNumex() {
        return expandNumex;
    }

    public void setExpandNumex(Boolean expandNumex) {
        this.expandNumex = expandNumex;
    }

    public Boolean getRomanNumerals() {
        return romanNumerals;
    }

    public void setRomanNumerals(Boolean romanNumerals) {
        this.romanNumerals = romanNumerals;
    }
}
