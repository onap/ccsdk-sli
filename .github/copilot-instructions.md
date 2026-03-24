# Copilot Instructions for This Repository

## Gerrit Review Queries

When the user asks to show open Gerrit reviews for this repository, do the following automatically:

1. Determine the Gerrit remote URL from git remote get-url gerrit.
2. Parse the remote to derive:
   - Gerrit host (for example gerrit.onap.org)
   - Gerrit project path after /r/ or :29418/ (for example ccsdk/sli)
3. Query Gerrit changes over HTTPS REST API:
   - Anonymous first: https://<host>/r/changes/?q=status:open+project:<project>&n=25
   - If anonymous fails due permissions, use authenticated endpoint:
     https://<host>/r/a/changes/?q=status:open+project:<project>&n=25
4. Strip the Gerrit JSON XSSI prefix line (the line beginning with )]}').
5. Summarize results in a compact table with at least:
   - Change number
   - Subject
   - Updated time
   - Owner account id or name if present
   - Unresolved comment count
   - Status
6. Sort so the oldest-updated open changes are easiest to identify.
7. If the user asks for more detail, include labels and submit records.

## Command Strategy

- Prefer curl and jq when available.
- If jq is unavailable, use shell tools to provide a readable fallback summary.
- Keep commands read-only and do not modify repository files for this task.

## Authentication Notes

If authenticated access is needed, prompt the user to configure Gerrit HTTP credentials and then use one of these patterns:

- curl -u <user>:<http_password> https://<host>/r/a/changes/?q=status:open+project:<project>&n=25
- or rely on a credential helper already configured for the host.

Do not print secrets into chat output.
