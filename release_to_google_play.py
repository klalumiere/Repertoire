#!/usr/bin/env python3

import os
import subprocess

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
    assert SERVICE_ACCOUNT_FILE, "Can't find Google Play Service Account JSON path."
    credentials = service_account.Credentials.from_service_account_file(SERVICE_ACCOUNT_FILE, scopes=SCOPES)
    credentials.refresh(Request())
    service = googleapiclient.discovery.build("androidpublisher", "v3", credentials=credentials)

    edit = service.edits().insert(body={}, packageName=APP_ID).execute()

    bundles = service.edits().bundles().upload(
            editId=edit["id"],
            packageName=APP_ID,
            media_body=MediaFileUpload(BUNDLE_FILE_PATH, mimetype="application/octet-stream")).execute()

    release_note = subprocess.check_output(["git", "log", "-1", "--pretty=%B"]).decode().replace("\n","")
    track_body = {
        "releases": [
            {
                "name": str(bundles["versionCode"]),
                "versionCodes": [ bundles["versionCode"] ],
                "status": "completed",
                "releaseNotes": [
                    {"language": "en-US", "text": release_note }
                ]
            }
        ]
    }
    tracks = service.edits().tracks().update(
            editId=edit["id"],
            track=TRACK,
            packageName=APP_ID,
            body=track_body).execute()

    commit = service.edits().commit(editId=edit["id"], packageName=APP_ID).execute()
    print(f"App released (commit {commit['id']})")
    print(tracks)

    return 0



if __name__ == "__main__":
    exit(main())
