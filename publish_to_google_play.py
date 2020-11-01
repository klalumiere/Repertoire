#!/usr/bin/env python3

import os

from google.auth.transport.requests import Request
from google.oauth2 import service_account
from googleapiclient.http import MediaFileUpload
import googleapiclient.discovery

APP_ID = "klalumiere.repertoire"
BUNDLE_FILE_PATH = "app/build/outputs/bundle/release/app-release.aab"
SCOPES = [ "https://www.googleapis.com/auth/androidpublisher" ]
SERVICE_ACCOUNT_FILE = os.getenv("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_PATH")
TRACK = "internal"

def main() -> int:
    credentials = service_account.Credentials.from_service_account_file(SERVICE_ACCOUNT_FILE, scopes=SCOPES)
    credentials.refresh(Request())
    service = googleapiclient.discovery.build("androidpublisher", "v3", credentials=credentials)

    edit = service.edits().insert(body={}, packageName=APP_ID).execute()

    service.edits().bundles().upload(
            editId=edit["id"],
            packageName=APP_ID,
            media_body=MediaFileUpload(BUNDLE_FILE_PATH, mimetype="application/octet-stream")).execute()

    service.edits().tracks().update(
            editId=edit["id"],
            track=TRACK,
            packageName=APP_ID).execute()

    commit = service.edits().commit(editId=edit["id"], packageName=APP_ID).execute()

    print(f"App published (commit {commit['id']})")
    return 0



if __name__ == "__main__":
    exit(main())
