package smile.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andre Kullmann
 */
public class StringUtils {

    private static final char WILD_CARD = '*';

	private static final char[] WILD_CARD_ARRAY = { WILD_CARD };

    public static String capitalize( String str ) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static List<String> split( String str, String delim ) {

        List<String> list = new ArrayList<String>();

        int j=0;

        for( int i=str.indexOf( delim, j ); i!=-1; i=str.indexOf( delim, j ) ) {

            String s = str.substring(j,i);

            if( isBlank(s) )
                continue;

            list.add(s);

            j=i+delim.length();
        }

        String s = str.substring(j);
        if( !isBlank(s) )
            list.add(s);

        return list;
    }

    /**
     * Simular to {@link String#split(java.lang.String)} without regex.
     * 
     * @param str
     * @param delim
     * @return
     */
    public static String[] split( String str, char delim ) {

        List<String> list = new ArrayList<String>();

        int j=0;

        for( int i=str.indexOf( delim, j ); i!=-1; i=str.indexOf( delim, j ) ) {

            String s = str.substring(j,i);

            if( isBlank(s) )
                continue;

            list.add(s);

            j=i+1;
        }

        String s = str.substring(j);
        if( !isBlank(s) )
            list.add(s);

        return list.toArray( new String[ list.size() ] );
    }

    public static boolean isBlank( String  s ) {
        return s == null || s.length() == 0 || s.trim().length() == 0;
    }

    
	public static boolean match( String str, String pattern ) {

		return match( str.toCharArray(), 0, pattern.toCharArray(), 0 );
	}

	private static boolean match( char[] str, int sIdx, char[] pattern, int pIdx ) {

		if( pattern[pIdx] == WILD_CARD ) {

			if( pattern.length == (pIdx-1) )
				return true;

			pIdx++;

			int i = indexOf( pattern, pIdx, WILD_CARD_ARRAY, 0, 1 );

			if( i == -1 )
				return equals( str, str.length- (pattern.length-pIdx), pattern, pIdx, pattern.length );

			int j = indexOf( str, sIdx, pattern, pIdx, i );

            return j != -1 && match(str, j, pattern, i);

        } else {

			int i = indexOf( pattern, pIdx, WILD_CARD_ARRAY, 0, 1 );

            if( i == -1 ) {

                return pattern.length - pIdx == str.length - sIdx && equals(str, sIdx, pattern, pIdx, pattern.length - 1);

            }

			boolean q = equals(str, sIdx, pattern, pIdx, i );

            return q && match(str, sIdx + (i - pIdx), pattern, i);

        }

	}

    private static boolean equals( char[] str, int strIdx, char[] pattern, int from, int to ) {


    	if( str.length < pattern.length )
    		return false;

        for( int i=0; i<to-from; i++) {

        	if( str[ strIdx + i] != pattern[ from+i ] ) {
                return false;
        	}
        }

        return true;
    }

	private static int indexOf( char[] str, int fromIdx, char[] pattern, int fromPattern, int toPattern ) {

		int patternLength = toPattern - fromPattern;

		for( int i=fromIdx; i<= str.length-patternLength; i++ ) {

			boolean found = true;

			for( int j=0; j<patternLength; j++ ) {

				if( str[i+j] != pattern[fromPattern+j] ) {
					found = false;
					break;
				}
			}

			if( found )
				return i;
		}

		return -1;
	}
}
