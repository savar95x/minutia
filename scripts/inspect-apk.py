#!/usr/bin/env python3
"""Sanity-check a built APK without a device: package, version, SDKs, signer, components, strings.

Usage:  pip install androguard  &&  python3 scripts/inspect-apk.py out/Minutia-1.8.0.apk [string ...]
Extra args are strings to look for in the dex (e.g. a new UI label) to confirm the code shipped.
"""
import sys, zipfile

EXPECTED_SIGNER = "3FE9CDB09C99EA32D6DEE5B657AA5A7DC37F3877232BD66D59E403B606F592D0"  # app/release.jks

try:
    from loguru import logger
    logger.remove()
except Exception:
    pass
from androguard.core.apk import APK

path = sys.argv[1]
a = APK(path)
print("package    ", a.get_package())
print("version    ", a.get_androidversion_name(), "code", a.get_androidversion_code())
print("sdk        ", "min", a.get_min_sdk_version(), "target", a.get_target_sdk_version())
certs = a.get_certificates_v2() or a.get_certificates()
for c in certs:
    fp = c.sha256_fingerprint.replace(" ", "").replace(":", "").upper()
    ok = "OK" if fp == EXPECTED_SIGNER else "MISMATCH: installs over the owner's copy will FAIL"
    print("signer     ", fp, ok)
print("signed v2/v3", a.is_signed_v2(), a.is_signed_v3())
print("activities ", a.get_activities())
print("receivers  ", a.get_receivers())
print("permissions", sorted(a.get_permissions()))
if len(sys.argv) > 2:
    z = zipfile.ZipFile(path)
    dex = b"".join(z.read(n) for n in z.namelist() if n.endswith(".dex"))
    for s in sys.argv[2:]:
        print(f"dex has {s!r}:", s.encode() in dex)
