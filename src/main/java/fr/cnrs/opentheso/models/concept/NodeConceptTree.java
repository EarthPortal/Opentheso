package fr.cnrs.opentheso.models.concept;

import java.text.Normalizer;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Cette Classe permet de gérer les noeuds de Concept dans l'arbre.
 * 
 * @author miled.rousset
 */
@Data
public class NodeConceptTree implements Comparable <NodeConceptTree>{

    private String title;
    private String idConcept;
    private String notation = "";
    private String idThesaurus;
    private String idLang;
    private String statusConcept;
    private boolean haveChildren = false;
    private boolean isGroup =false;
    private boolean isSubGroup = false;
    private boolean isTopTerm =false;
    private boolean isTerm =false;
    private boolean isFacet = false;
    private boolean isDeprecated = false;
       
    public NodeConceptTree() {
        this.title = "";
    }

    @Override
    public int compareTo(NodeConceptTree o) {
        if (this.title == null && o.title == null) return 0;
        if (this.title == null) return 1;
        if (o.title == null) return -1;

        String str1 = normalize(this.title);
        String str2 = normalize(o.title);

        return naturalCompare(str1, str2, true);
    }

    private String normalize(String s) {
        if (s == null) return "";

        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
    }

    public int naturalCompare(String a, String b, boolean ignoreCase) {
        if (ignoreCase) {
            a = a.toLowerCase();
            b = b.toLowerCase();
        }
        int aLength = a.length();
        int bLength = b.length();
        int minSize = Math.min(aLength, bLength);
        char aChar, bChar;
        boolean aNumber, bNumber;
        boolean asNumeric = false;
        int lastNumericCompare = 0;
        for (int i = 0; i < minSize; i++) {
            aChar = a.charAt(i);
            bChar = b.charAt(i);
            aNumber = aChar >= '0' && aChar <= '9';
            bNumber = bChar >= '0' && bChar <= '9';
            if (asNumeric)
                if (aNumber && bNumber) {
                    if (lastNumericCompare == 0)
                        lastNumericCompare = aChar - bChar;
                } else if (aNumber)
                    return 1;
                else if (bNumber)
                    return -1;
                else if (lastNumericCompare == 0) {
                    if (aChar != bChar)
                        return aChar - bChar;
                    asNumeric = false;
                } else
                    return lastNumericCompare;
            else if (aNumber && bNumber) {
                asNumeric = true;
                if (lastNumericCompare == 0)
                    lastNumericCompare = aChar - bChar;
            } else if (aChar != bChar)
                return aChar - bChar;
        }
        if (asNumeric)
            if (aLength > bLength && a.charAt(bLength) >= '0' && a.charAt(bLength) <= '9') // as number
                return 1;  // a has bigger size, thus b is smaller
            else if (bLength > aLength && b.charAt(aLength) >= '0' && b.charAt(aLength) <= '9') // as number
                return -1;  // b has bigger size, thus a is smaller
            else if (lastNumericCompare == 0)
              return aLength - bLength;
            else
                return lastNumericCompare;
        else
            return aLength - bLength;
    }    
}
