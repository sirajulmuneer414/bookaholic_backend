use library_db;
-- Fix corrupted book availability counts
-- This recalculates available_copies based on actual borrow records

-- Step 1: Show books with incorrect available counts (for verification)
SELECT 
    id,
    title,
    total_copies,
    available_copies as current_available,
    (total_copies - (
        SELECT COUNT(*) 
        FROM borrow_records 
        WHERE book_id = books.id 
        AND status = 'BORROWED'
    )) as should_be_available,
    (SELECT COUNT(*) FROM borrow_records WHERE book_id = books.id AND status = 'BORROWED') as currently_borrowed
FROM books
WHERE available_copies != (total_copies - (
    SELECT COUNT(*) 
    FROM borrow_records 
    WHERE book_id = books.id 
    AND status = 'BORROWED'
))
ORDER BY id;

-- Step 2: Fix all books by recalculating available_copies
-- Formula: available_copies = total_copies - (count of BORROWED records)
-- Note: Using id >= 0 to satisfy MySQL safe update mode (uses primary key)
UPDATE books
SET available_copies = (
    total_copies - (
        SELECT COUNT(*) 
        FROM borrow_records 
        WHERE book_id = books.id 
        AND status = 'BORROWED'
    )
)
WHERE id >= 0  -- Uses primary key to satisfy MySQL safe update mode
AND available_copies != (
    total_copies - (
        SELECT COUNT(*) 
        FROM borrow_records 
        WHERE book_id = books.id 
        AND status = 'BORROWED'
    )
);

-- Step 3: Verify the fix - should return 0 rows after fix
SELECT 
    id,
    title,
    total_copies,
    available_copies,
    (SELECT COUNT(*) FROM borrow_records WHERE book_id = books.id AND status = 'BORROWED') as borrowed_count
FROM books
WHERE available_copies > total_copies
   OR available_copies < 0
ORDER BY id;
