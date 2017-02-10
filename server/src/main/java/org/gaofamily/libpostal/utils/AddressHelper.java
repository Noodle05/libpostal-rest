package org.gaofamily.libpostal.utils;

import com.mapzen.jpostal.ExpanderOptions;
import org.gaofamily.libpostal.server.options.ExpanderOptionsJson;

/**
 * @author Wei Gao
 * @since 8/10/16
 */
public abstract class AddressHelper {
    public static ExpanderOptions toExpanderOptions(ExpanderOptionsJson json) {
        ExpanderOptions.Builder builder = new ExpanderOptions.Builder();
        if (json.getLanguages() != null) {
            builder.languages(json.getLanguages());
        }
        if (json.getAddressComponents() != null) {
            builder.addressComponents(json.getAddressComponents());
        }
        if (json.getLatinAscii() != null) {
            builder.latinAscii(json.getLatinAscii());
        }
        if (json.getTransliterate() != null) {
            builder.transliterate(json.getTransliterate());
        }
        if (json.getStripAccents() != null) {
            builder.stripAccents(json.getStripAccents());
        }
        if (json.getDecompose() != null) {
            builder.decompose(json.getDecompose());
        }
        if (json.getLowercase() != null) {
            builder.lowercase(json.getLowercase());
        }
        if (json.getTrimString() != null) {
            builder.trimString(json.getTrimString());
        }
        if (json.getDropParentheticals() != null) {
            builder.dropParentheticals(json.getDropParentheticals());
        }
        if (json.getReplaceNumericHyphens() != null) {
            builder.replaceNumericHyphens(json.getReplaceNumericHyphens());
        }
        if (json.getDeleteNumericHyphens() != null) {
            builder.deleteNumericHyphens(json.getDeleteNumericHyphens());
        }
        if (json.getSplitAlphaFromNumeric() != null) {
            builder.splitAlphaFromNumeric(json.getSplitAlphaFromNumeric());
        }
        if (json.getReplaceWordHyphens() != null) {
            builder.replaceWordHyphens(json.getReplaceWordHyphens());
        }
        if (json.getDeleteWordHyphens() != null) {
            builder.deleteWordHyphens(json.getDeleteWordHyphens());
        }
        if (json.getDeleteFinalPeriods() != null) {
            builder.deleteFinalPeriods(json.getDeleteFinalPeriods());
        }
        if (json.getDeleteAcronymPeriods() != null) {
            builder.deleteAcronymPeriods(json.getDeleteAcronymPeriods());
        }
        if (json.getDropEnglishPossessives() != null) {
            builder.dropEnglishPossessives(json.getDropEnglishPossessives());
        }
        if (json.getDeleteApostrophes() != null) {
            builder.deleteApostrophes(json.getDeleteApostrophes());
        }
        if (json.getExpandNumex() != null) {
            builder.expandNumex(json.getExpandNumex());
        }
        if (json.getRomanNumerals() != null) {
            builder.romanNumerals(json.getRomanNumerals());
        }
        return builder.build();
    }
}
