pragma solidity ^0.6.10;

contract AgriCertificateEvidence {
    struct Evidence {
        string businessCode;
        string evidenceType;
        string evidenceHash;
        string relatedCode;
        string operatorId;
        uint256 timestamp;
        bool exists;
    }

    mapping(string => Evidence) private evidences;
    mapping(string => bool) private revokedCertificates;

    event EvidenceSaved(string businessCode, string evidenceType, uint256 timestamp);
    event CertificateRevoked(string certificateCode, uint256 timestamp);

    function saveEvidence(
        string memory businessCode,
        string memory evidenceType,
        string memory evidenceHash,
        string memory relatedCode,
        string memory operatorId
    ) public {
        require(!evidences[businessCode].exists, "Evidence already exists");
        evidences[businessCode] = Evidence(
            businessCode,
            evidenceType,
            evidenceHash,
            relatedCode,
            operatorId,
            block.timestamp,
            true
        );
        emit EvidenceSaved(businessCode, evidenceType, block.timestamp);
    }

    function getEvidence(string memory businessCode) public view returns (
        string memory, string memory, string memory, string memory, string memory, uint256, bool
    ) {
        Evidence memory e = evidences[businessCode];
        require(e.exists, "Evidence not found");
        return (e.businessCode, e.evidenceType, e.evidenceHash, e.relatedCode, e.operatorId, e.timestamp, e.exists);
    }

    function revokeCertificate(
        string memory certificateCode,
        string memory revokeHash,
        string memory operatorId
    ) public {
        require(!revokedCertificates[certificateCode], "Already revoked");
        revokedCertificates[certificateCode] = true;
        string memory revokeCode = string(abi.encodePacked(certificateCode, "_REVOKE"));
        if (!evidences[revokeCode].exists) {
            evidences[revokeCode] = Evidence(
                revokeCode,
                "REVOKE",
                revokeHash,
                certificateCode,
                operatorId,
                block.timestamp,
                true
            );
        }
        emit CertificateRevoked(certificateCode, block.timestamp);
    }

    function isCertificateRevoked(string memory certificateCode) public view returns (bool) {
        return revokedCertificates[certificateCode];
    }
}
