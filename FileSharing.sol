pragma solidity ^0.4.24;


contract FileSharing {

    struct File {
        bytes32 hash;
        string[] keywords;
        address author;
        uint256 id;
        string description;
        uint256 grade;
    }

    uint256 fileCount;
    File[] uploadedFiles;
    mapping(bytes32 => uint256[]) keywordToFiles;
    mapping(address => uint256[]) authorToFiles;

    mapping(address => string[]) public requests;
    mapping(address => uint256) public requestsStack;

    modifier isAuthor(uint256 _fileId) {
        require(getAuthor(_fileId) == msg.sender);
        _;
    }

    modifier isNotAuthor(uint256 _fileId) {
        require(getAuthor(_fileId) != msg.sender);
        _;
    }

    modifier isInRange(uint256 _fileId, uint256 _keywordId) {
        require(_keywordId >= 0 && _keywordId < numberOfKeywords(_fileId));
        _;
    }

    function hash(string memory _word) pure private returns (bytes32) {
        return sha256(abi.encodePacked(_word));
    }

    function getFile(uint256 _fileId) private view returns (File storage) {
        return uploadedFiles[_fileId];
    }


    function upload(bytes32 _hash, string _keyword1, string _keyword2, string _keyword3, string _description) external returns (uint256 id) {

        string[] memory keywords = new string[](3);
        keywords[0] = _keyword1;
        keywords[1] = _keyword2;
        keywords[2] = _keyword3;
        File memory file = File(_hash, keywords, msg.sender, fileCount, _description, 0);
        uploadedFiles.push(file);
        keywordToFiles[hash(_keyword1)].push(fileCount);
        keywordToFiles[hash(_keyword2)].push(fileCount);
        keywordToFiles[hash(_keyword3)].push(fileCount);
        authorToFiles[msg.sender].push(fileCount);
        id = fileCount;
        fileCount += 1;

    }

    function addKeyword(string _keyword, uint256 _fileId) isAuthor(_fileId) external {
        uploadedFiles[_fileId].keywords.push(_keyword);
        keywordToFiles[hash(_keyword)].push(_fileId);
    }

    function editDescription(string _description, uint256 _fileId) isAuthor(_fileId) external {
        uploadedFiles[_fileId].description = _description;
    }

    function vote(uint256 _fileId, bool _good) external isNotAuthor(_fileId) {
        if (_good) {
            uploadedFiles[_fileId].grade += 1;
        } else {
            uploadedFiles[_fileId].grade -= 1;
        }
    }


    function fileHashesForKeyword(string  _keyword) view external returns (bytes32[] memory hashes) {
        uint256[] memory ids =  keywordToFiles[hash(_keyword)];
        uint256 length = ids.length;
        hashes = new bytes32[](length);

        for (uint256 i = 0; i < length; i++) {
            hashes[i] = getHash(ids[i]);
        }
    }

    function fileIdsForKeyword(string _keyword) view external returns (uint256[] memory ids) {
        ids = keywordToFiles[hash(_keyword)];
    }

    function fileHashesForAuthor(address _author) view external returns (bytes32[] memory hashes) {
    uint256[] memory ids = authorToFiles[_author];
    uint256 length = ids.length;
        hashes = new bytes32[](length);
        for (uint256 i = 0; i < length; i++) {
            hashes[i] = getHash(ids[i]);
        }
    }

    function fileIdsForAuthor(address _author) view external returns (uint256[] memory ids) {
        ids = authorToFiles[_author];
    }

    function numberOfKeywords(uint256 _fileId) view public returns (uint256) {
        return getFile(_fileId).keywords.length;
    }

    function getFileDescription(uint256 _fileId) view public returns (string memory) {
        return getFile(_fileId).description;
    }

    function getKeyword(uint256 _fileId, uint256 _keywordId) view isInRange(_fileId, _keywordId) public returns (string memory) {
        return getFile(_fileId).keywords[_keywordId];
    }

    function getAuthor(uint256 _fileId) view public returns (address) {
        return getFile(_fileId).author;
    }

    function getHash(uint256 _fileId) view public returns (bytes32) {
        return getFile(_fileId).hash;
    }

    function getGrade(uint256 _fileId) view public returns (uint256) {
        return getFile(_fileId).grade;
    }

    function getFileCount() view public returns (uint256) {
        return fileCount;
    }

}
