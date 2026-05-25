/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

// Initialize PrimeFaces locales object immediately (before PrimeFaces loads)
// This must be executed synchronously, before any PrimeFaces components initialize
(function() {
    // Create PrimeFaces object if it doesn't exist
    // IMPORTANT: Only create structure, don't override existing functions
    if (typeof window.PrimeFaces === 'undefined') {
        window.PrimeFaces = {};
    }
    
    // Create locales object if it doesn't exist
    if (typeof window.PrimeFaces.locales === 'undefined') {
        window.PrimeFaces.locales = {};
    }
    
    // Create settings object if it doesn't exist (but don't override if already exists)
    if (typeof window.PrimeFaces.settings === 'undefined') {
        window.PrimeFaces.settings = {};
    }
    
    // Create widget object if it doesn't exist (but don't override if already exists)
    if (typeof window.PrimeFaces.widget === 'undefined') {
        window.PrimeFaces.widget = {};
    }
    
    // Create stub functions ONLY if PrimeFaces is not yet loaded
    // These stubs prevent errors if PrimeFaces core.js tries to call them before full initialization
    // PrimeFaces will override these when it fully loads
    var isPrimeFacesLoaded = typeof window.PrimeFaces !== 'undefined' && 
                             typeof window.PrimeFaces.createWidget === 'function';
    
    if (!isPrimeFacesLoaded) {
        // PrimeFaces not fully loaded yet - create safe stubs
        if (typeof window.PrimeFaces.debug === 'undefined') {
            window.PrimeFaces.debug = function() {
                // Stub - will be overridden by PrimeFaces
            };
        }
        
        if (typeof window.PrimeFaces.cw === 'undefined') {
            window.PrimeFaces.cw = function() {
                // Stub - will be overridden by PrimeFaces
                return null;
            };
        }
        
        if (typeof window.PrimeFaces.ab === 'undefined') {
            window.PrimeFaces.ab = function() {
                // Stub - will be overridden by PrimeFaces
                return false;
            };
        }
    }
    
    // Create debug stub if it doesn't exist (PrimeFaces will override it later)
    if (typeof window.PrimeFaces.debug === 'undefined') {
        window.PrimeFaces.debug = function() {
            // Stub function - PrimeFaces will override this when it loads
        };
    }

    // Define English (US) locale for PrimeFaces 6.1 with calendar property
    window.PrimeFaces.locales['en_US'] = {
        closeText: 'Close',
        prevText: 'Previous',
        nextText: 'Next',
        monthNames: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
        monthNamesShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
        dayNames: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
        dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
        dayNamesMin: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
        weekHeader: 'Week',
        firstDay: 1,
        isRTL: false,
        showMonthAfterYear: false,
        yearSuffix: '',
        timeOnlyTitle: 'Time Only',
        timeText: 'Time',
        hourText: 'Hour',
        minuteText: 'Minute',
        secondText: 'Second',
        currentText: 'Today',
        ampm: false,
        month: 'Month',
        week: 'Week',
        day: 'Day',
        allDayText: 'All Day',
        calendar: {
            BUTTON: 'Show Calendar',
            TODAY: 'Today',
            CLEAR: 'Clear',
            CLOSE: 'Close',
            PREV_MONTH: 'Previous Month',
            NEXT_MONTH: 'Next Month',
            PREV_YEAR: 'Previous Year',
            NEXT_YEAR: 'Next Year',
            SELECT_MONTH: 'Select Month',
            SELECT_YEAR: 'Select Year',
            SELECT_DATE: 'Select Date',
            SELECT_TIME: 'Select Time'
        },
        datatable: {
            sort: {
                ASC: 'Sort Ascending',
                DESC: 'Sort Descending',
                NONE: 'Remove Sorting'
            },
            filter: {
                ALL: 'All',
                MATCH_MODE: 'Match Mode',
                STartsWith: 'Starts with',
                CONTAINS: 'Contains',
                NOT_CONTAINS: 'Not contains',
                ENDS_WITH: 'Ends with',
                EQUALS: 'Equals',
                NOT_EQUALS: 'Not equals',
                LT: 'Less than',
                LTE: 'Less than or equal to',
                GT: 'Greater than',
                GTE: 'Greater than or equal to',
                DATE_IS: 'Date is',
                DATE_IS_NOT: 'Date is not',
                DATE_BEFORE: 'Date is before',
                DATE_AFTER: 'Date is after'
            },
            row: {
                SELECT: 'Select',
                SELECT_ALL: 'Select All',
                UNSELECT_ALL: 'Unselect All',
                EXPAND: 'Expand',
                COLLAPSE: 'Collapse'
            },
            paginator: {
                FIRST: 'First',
                PREVIOUS: 'Previous',
                NEXT: 'Next',
                LAST: 'Last',
                CURRENT_PAGE_REPORT: '{currentPage} of {totalPages}',
                ROWS_PER_PAGE: '{rowsPerPage} rows per page',
                OF: 'of',
                PAGE: 'Page',
                START_RECORD: '{startRecord} - {endRecord} of {totalRecords}',
                EMPTY_MESSAGE: 'No records found'
            },
            emptyMessage: 'No records found',
            selectionMessage: '{0} selected',
            totalRecordsMessage: '{0} total records'
        }
    };
    
    // Set locale in settings (will be used when PrimeFaces loads)
    window.PrimeFaces.settings.locale = 'en_US';
    
    // Wait for PrimeFaces to fully load and then ensure locale is set
    if (typeof window.jQuery !== 'undefined') {
        window.jQuery(document).ready(function() {
            if (typeof window.PrimeFaces !== 'undefined' && window.PrimeFaces.settings) {
                window.PrimeFaces.settings.locale = 'en_US';
            }
        });
    }
})();
