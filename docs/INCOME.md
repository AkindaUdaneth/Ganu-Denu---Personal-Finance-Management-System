# Income overview and Add Income

Branch: `feature/income-dashboard`.

## User flow

The Income Dashboard is an overview only: confirmed/estimated totals, source cards, sync/error states and the + Log Income button. Its source cards have no click action or input fields.

`Income overview -> + Log Income -> Add Income -> Save Income -> Income overview`

Add Income is a separate navigation destination. Bottom navigation remains visible with Income selected. Its four compact cards select Salary, Freelance, Google AdSense or Cryptocurrency. The selected card has a yellow border. Switching cards preserves the same amount, date, frequency and status inputs and changes only the source written to the record.

## Common form

All four sources use the same fields:

- Amount (LKR): positive value with up to two decimal places; properly grouped commas such as 135,000.00 are accepted.
- Payment Date: a calendar picker. Confirmed cash receipts cannot be future-dated; estimates can.
- Frequency: One-off, Daily, Weekly, Monthly or Yearly. Monthly is the form default.
- Income Status: Confirmed or Estimated.

The form logs one record per Save; frequency is metadata and does not automatically generate future receipts. After a successful save the screen returns to the overview, which updates through its existing listener. Records outside the displayed month are saved with an explanatory message.

New entries on this page are cash amounts in LKR for every source, including Cryptocurrency. Existing signed, non-liquid crypto P&L records remain supported and are excluded from cash totals. When both cash receipts and P&L exist, the crypto card shows confirmed cash and identifies P&L separately. The model retains support for original-currency legacy records, but this common form does not ask different currency/conversion questions for different sources.

## Architecture

`IncomeDashboardScreen / AddIncomeScreen -> shared IncomeViewModel -> IncomeRepository -> Firestore`

The overview and form share the Income overview navigation entry's ViewModel. GanuDenuApplication initializes the shared Auth, Income and Settings dependencies. GanuDenuApp opens finance navigation only after login/signup and a saved profile; logout disposes that user navigation and returns to login. Firebase income records are scoped to the same signed-in UID.

A month-scoped real-time query reads `users/{auth.uid}/incomes` using the device's local calendar/timezone. Listener cleanup, cache/pending metadata, empty states and errors are handled. Malformed records are skipped with an incomplete-totals warning. Production never seeds the screenshot's sample money.

Each form receives one document ID retained on failed/time-out retries, avoiding duplicate records when retrying that form. After a 25-second timeout, the write may still sync; keep the form and retry rather than entering it as a new record. Cached pending entries can roll back if rejected by server rules.

## Stored schema

Path: `users/{uid}/incomes/{draftId}`

| Field | Type / meaning |
| --- | --- |
| source | salary / freelance / adsense / cryptocurrency; selected source card |
| status | confirmed / estimated; legacy pnl supported for crypto |
| originalAmount | decimal string; normalized without grouping commas |
| currency | LKR for new common-form entries; legacy USD/USDT/ETH supported |
| exchangeRateToLkr | 1 for LKR; historical rate retained for legacy conversions |
| amountLkrMinor | integer LKR minor units |
| occurredAt | Firestore Timestamp for payment/expected date |
| frequency | one_off / daily / weekly / monthly / yearly |
| note | optional legacy note, empty for this common form |
| createdAt | server timestamp of the latest save for that draft ID |

Existing records without frequency are read as One-off. New saves require frequency. The date range/order use the same occurredAt field, so no composite index is needed.

## Rules and verification

Publish the updated firestore.rules before saving from the new form. Rules enforce user ownership, allowed fields, amount bounds and source/status/currency/frequency values. The broad wildcard rule excludes incomes, so it cannot bypass the income-specific validation. Other users' data remains inaccessible.

```powershell
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest
./gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.sliit.se3092.ganudenu.IncomeDashboardTest,com.sliit.se3092.ganudenu.AddIncomeTest,com.sliit.se3092.ganudenu.IncomeNavigationTest'
```

Tests cover cash/estimate/P&L totals, conversion, grouped amounts, validation, source selection preserving inputs, all four sources saving distinct records with frequency, safe retries, overview cards being non-interactive, Add Income rendering and the complete navigation/save/return flow. Emulator tests use a fake repository and do not create Firebase records. Test screenshots are collected in app/build/outputs/connected_android_test_additional_output.

For a live check, publish rules, sign in through the group's auth integration, add one entry for each source and confirm the matching source/frequency in Firestore. Check an estimated entry separately from confirmed totals, another account's isolation, and reconnect/sync behavior. Local rules are not automatically deployed.
