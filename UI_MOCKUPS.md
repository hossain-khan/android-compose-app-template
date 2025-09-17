# UI Screenshots and Mockups

Since this is a code implementation without an Android emulator, here are detailed descriptions of the new UI screens:

## 1. Main Email Screen (Enhanced Inbox)

```
┌─────────────────────────────────────────┐
│ Email                              [i]  │  ← TopAppBar with info icon
├─────────────────────────────────────────┤
│ [Inbox] [Drafts] [Sent]                 │  ← Material 3 Tab Row
├─────────────────────────────────────────┤
│ 👤 Ali Connors              3:00 PM     │  ← Email items in LazyColumn
│    Meeting re-sched!                    │
│    Hey, I'm going to be out of...       │
├─────────────────────────────────────────┤
│ 👤 John Doe                 4:00 PM     │
│    Team retro                           │
│    Don't forget about the team...       │
├─────────────────────────────────────────┤
│ 👤 Jane Smith               5:00 PM     │
│    Upcoming Holiday Event               │
│    Join us for the upcoming...          │
├─────────────────────────────────────────┤
│ ... more emails ...                     │
│                                         │
│                                   [+]   │  ← Floating Action Button
└─────────────────────────────────────────┘
```

## 2. Compose Email Screen

```
┌─────────────────────────────────────────┐
│ [<] Compose Email                       │  ← TopAppBar with back arrow
├─────────────────────────────────────────┤
│                                         │
│ To: [email@example.com_____________]    │  ← OutlinedTextField
│                                         │
│ Subject: [____________________]         │  ← OutlinedTextField
│                                         │
│ Message:                                │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │  ← Large multiline TextField
│ │ Type your message here...           │ │
│ │                                     │ │
│ │                                     │ │
│ │                                     │ │
│ │                                     │ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ [Save Draft]           [Send]           │  ← Action buttons
└─────────────────────────────────────────┘
```

## 3. Drafts Tab View

```
┌─────────────────────────────────────────┐
│ Email                              [i]  │
├─────────────────────────────────────────┤
│ [Inbox] [Drafts] [Sent]                 │  ← Drafts tab selected
├─────────────────────────────────────────┤
│ 👤 Me                    Draft          │  ← Draft email entries
│    Important presentation               │
│    Need to prepare slides for...        │
├─────────────────────────────────────────┤
│ 👤 Me                    Draft          │
│    Follow up email                      │
│    Remember to follow up on...          │
├─────────────────────────────────────────┤
│ (Empty state when no drafts)            │
│                                         │
│                                   [+]   │
└─────────────────────────────────────────┘
```

## 4. Sent Tab View

```
┌─────────────────────────────────────────┐
│ Email                              [i]  │
├─────────────────────────────────────────┤
│ [Inbox] [Drafts] [Sent]                 │  ← Sent tab selected
├─────────────────────────────────────────┤
│ 👤 Me                    2:30 PM        │  ← Sent email entries
│    Project update                       │
│    Hi team, here's the latest...        │
├─────────────────────────────────────────┤
│ 👤 Me                    1:15 PM        │
│    Meeting confirmation                 │
│    Thanks for confirming the...         │
├─────────────────────────────────────────┤
│ (Empty state when no sent emails)       │
│                                         │
│                                   [+]   │
└─────────────────────────────────────────┘
```

## User Flow

1. **Starting Point**: User sees main screen with Inbox tab selected showing received emails
2. **Compose New Email**: User taps FAB (+) → Opens Compose Email Screen
3. **Fill Form**: User enters recipients, subject, and message
4. **Save or Send**: User can either:
   - Tap "Save Draft" → Returns to main screen, email appears in Drafts tab
   - Tap "Send" → Returns to main screen, email appears in Sent tab
5. **Edit Draft**: User switches to Drafts tab, taps draft → Opens Compose screen with pre-filled data
6. **View Sent**: User switches to Sent tab to see previously sent emails

## Key UI Features

- **Material 3 Design**: Uses latest Material Design components
- **Responsive Layout**: Proper spacing and padding throughout
- **Icon Consistency**: Person icons for all email senders
- **Tab Navigation**: Clear visual indication of active tab
- **Form Validation**: Send button disabled until required fields filled
- **Smart Timestamps**: Drafts show "Draft" status, sent emails show send time

This UI provides a complete email management experience with intuitive navigation and clean Material Design aesthetics.