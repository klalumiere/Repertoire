import subprocess

from googleapiclient.http import MediaFileUpload
import google.auth
import googleapiclient.discovery

APP_ID = "klalumiere.repertoire"
BUNDLE_FILE_PATH = "app/build/outputs/bundle/release/app-release.aab"
SCOPES = [ "https://www.googleapis.com/auth/androidpublisher" ]
TRACK = "internal"

def main() -> int:
    credentials, _ = google.auth.default()
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
