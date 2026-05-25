/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import web.entity.ce.Case;

@Configurable
public class SumTranslator {

    public String sumToString(BigDecimal sum, Case integralCase, Case fractionCase) {
        return String.join(" ", sumToList(sum, integralCase, fractionCase));
    }

    private List<String> sumToList(BigDecimal sum, Case integralCase, Case fractionCase) {
        String integralPlural = Optional.ofNullable(integralCase.getPlural()).map(String::toLowerCase).orElse("");
        String fractionPlural = Optional.ofNullable(fractionCase.getPlural()).map(String::toLowerCase).orElse("");
        if (Objects.isNull(sum)) {
            return Arrays.asList("zero " + integralPlural, "00 " + fractionPlural);
        }
        List<String> wholeWords = new ArrayList<>();
        BigDecimal sumInput = sum;
        if (sum.signum() < 0) {
            wholeWords.add("minus");
            sumInput = sum.negate();
        }
        String integralGenitive = Optional.ofNullable(integralCase.getGenitive()).map(String::toLowerCase).orElse("");
        String integralNominative = Optional.ofNullable(integralCase.getNominative()).map(String::toLowerCase).orElse("");
        String fractionGenitive = Optional.ofNullable(fractionCase.getGenitive()).map(String::toLowerCase).orElse("");
        String fractionNominative = Optional.ofNullable(fractionCase.getNominative()).map(String::toLowerCase).orElse("");
        String[] parts = sumInput.toString().split("\\.");
        wholeWords.addAll(wholeToWords(parts[0], integralPlural, integralGenitive, integralNominative));
        String fractional = "00";
        if (parts.length > 1) {
            fractional = parts[1];
            for (int i = 0; i < 2 - parts[1].length(); i++) {
                fractional += '0';
            }
        }
        return Arrays.asList(String.join(" ", wholeWords), fractionalToWords(fractional, fractionPlural, fractionGenitive, fractionNominative));
    }

    private String fractionalToWords(String number, String plural, String genitive, String nominative) {
        String result = number + " ";
        switch (number.charAt(1)) {
            case '1':
                result += number.charAt(0) != '1' ? nominative : plural;
                break;
            case '2':
            case '3':
            case '4':
                result += number.charAt(0) != '1' ? genitive : plural;
                break;
            default:
                result += plural;
        }
        return result;
    }

    private List<String> wholeToWords(String number, String plural, String genitive, String nominative) {
        Stack<ThreeChar> threeChars = new Stack<>();
        threeChars.push(new ThreeChar());
        for (int i = 0; i < number.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                threeChars.push(new ThreeChar());
            }
            char curChar = number.charAt(number.length() - 1 - i);
            ThreeChar threeChar = threeChars.peek();
            switch (i) {
                case 0:
                    threeChar.range = Ranges.NON;
                    threeChar.unit = curChar;
                    break;
                case 3:
                    threeChar.range = Ranges.THOUSANDS;
                    threeChar.unit = curChar;
                    break;
                case 6:
                    threeChar.range = Ranges.MILLIONS;
                    threeChar.unit = curChar;
                    break;
                case 9:
                    threeChar.range = Ranges.BILLIONS;
                    threeChar.unit = curChar;
                    break;
                case 2:
                case 5:
                case 8:
                case 11:
                    threeChar.hundred = curChar;
                    break;
                default:
                    threeChar.decade = curChar;
            }
        }
        List<String> result = new ArrayList<>();
        while (!threeChars.isEmpty()) {
            ThreeChar threeChar = threeChars.pop();
            if (threeChar.hundred > '0') {
                result.add(getHundreds(threeChar.hundred));
            }
            if (threeChar.decade > '0') {
                result.add((threeChar.decade > '1' || (threeChar.decade == '1' && threeChar.unit == '0')) ? getDecades(threeChar.decade) :
                           getTeens(threeChar.unit));
            }
            if (threeChar.unit > '0' && threeChar.decade != '1') {
                result.add(getUnits(threeChar.unit, threeChar.range == Ranges.THOUSANDS));
            }
            switch (threeChar.range) {
                case BILLIONS:
                    // billions / billion
                    result.add(threeCharToString(threeChar, "billions", "billions", "billion"));
                    break;
                case MILLIONS:
                    // millions / million
                    result.add(threeCharToString(threeChar, "millions", "millions", "million"));
                    break;
                case THOUSANDS:
                    // thousands / thousand
                    result.add(threeCharToString(threeChar, "thousands", "thousands", "thousand"));
                    break;
                default:
                    result.add(threeCharToString(threeChar, plural, genitive, nominative));
            }
        }
        return result;
    }

    private String getHundreds(char dig) {
        String text = null;
        switch (dig) {
            case '1':
                text = "one hundred";
                break;
            case '2':
                text = "two hundred";
                break;
            case '3':
                text = "three hundred";
                break;
            case '4':
                text = "four hundred";
                break;
            case '5':
                text = "five hundred";
                break;
            case '6':
                text = "six hundred";
                break;
            case '7':
                text = "seven hundred";
                break;
            case '8':
                text = "eight hundred";
                break;
            case '9':
                text = "nine hundred";
                break;
        }
        return text;
    }

    private String getDecades(char dig) {
        String text = null;
        switch (dig) {
            case '1':
                text = "ten";
                break;
            case '2':
                text = "twenty";
                break;
            case '3':
                text = "thirty";
                break;
            case '4':
                text = "forty";
                break;
            case '5':
                text = "fifty";
                break;
            case '6':
                text = "sixty";
                break;
            case '7':
                text = "seventy";
                break;
            case '8':
                text = "eighty";
                break;
            case '9':
                text = "ninety";
                break;
        }
        return text;
    }

    private String getUnits(char dig, boolean female) {
        String text = null;
        switch (dig) {
            case '1':
                text = "one";
                break;
            case '2':
                text = "two";
                break;
            case '3':
                text = "three";
                break;
            case '4':
                text = "four";
                break;
            case '5':
                text = "five";
                break;
            case '6':
                text = "six";
                break;
            case '7':
                text = "seven";
                break;
            case '8':
                text = "eight";
                break;
            case '9':
                text = "nine";
                break;
        }
        return text;
    }

    private String getTeens(char dig) {
        String text = "";
        switch (dig) {
            case '1':
                text = "eleven";
                break;
            case '2':
                text = "twelve";
                break;
            case '3':
                text = "thirteen";
                break;
            case '4':
                text = "fourteen";
                break;
            case '5':
                text = "fifteen";
                break;
            case '6':
                text = "sixteen";
                break;
            case '7':
                text = "seventeen";
                break;
            case '8':
                text = "eighteen";
                break;
            case '9':
                text = "nineteen";
                break;
        }
        return text;
    }

    private String threeCharToString(ThreeChar threeChar, String plural, String genitive, String nominative) {
        return (threeChar.decade == '1' || threeChar.unit == '0' || threeChar.unit > '4') ? plural : (threeChar.unit > '1') ? genitive : nominative;
    }

    private enum Ranges {
        NON,
        THOUSANDS,
        MILLIONS,
        BILLIONS
    }

    @Data
    private class ThreeChar {

        private char hundred;

        private char decade;

        private char unit;

        private Ranges range;
    }
}
