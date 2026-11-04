# Ganu Denu - End-to-End Test Cases

## 1. Impulse Checker Test
*   **Action:** Navigate to Goal Details. Enter `15000` in Impulse Checker.
*   **Expected Result:** Calculates delay days based on required monthly savings.
*   **Status:** [x] Passed

## 2. Total Offline Restoration Test
*   **Action:** Turn off Wi-Fi. Edit Goal name and saved amount.
*   **Expected Result:** Dashboard updates via Room cache.
*   **Action 2:** Restart app with Wi-Fi on.
*   **Expected Result:** App syncs offline edits to Firestore in the background.
*   **Status:** [x] Passed

## 3. Income Aggregation Test
*   **Action:** Add LKR 100,000 (Confirmed) and LKR 20,000 (Estimated) in Income tab.
*   **Expected Result:** Dashboard Current Balance shows 100,000; Estimated shows 20,000.
*   **Status:** [x] Passed
