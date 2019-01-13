pragma solidity ^0.4.24;


contract Connection {

    uint256 userCount;
    address owner;

    struct ListElement{
        uint256 userId;
        uint256 previousUserId;
        uint256 nextUserId;
    }

    uint32 public numberOfPopUsers;
    mapping(uint256 => ListElement) public userStructs;
    uint256 private lastPopUserId;

    struct User {
        uint256 userId;
        string publicKey;
        bool online;
        bool populated;
        address ethAdress;
    }


    User[] userList;


    mapping(address => uint256) public userIdsList;
    mapping(address => string[]) public requests;
    mapping(address => uint256) public requestsStack;


    constructor() public {
        owner = msg.sender;
    }

    modifier newUser() {
        require(userIdsList[msg.sender] == 0);
        _;

    }

    modifier oldUser() {
        require(userIdsList[msg.sender] != 0);
        _;
    }

    modifier isOnline() {
        require(userList[userIdsList[msg.sender] - 1].online == true);
        _;
    }

    function getDataOfPopulated(uint256 _id) isOnline userPop(_id) view public returns (address, string) {
            return (getAddressOf(_id), getPublicKeyOf(_id));

    }


    function register(string _publicKey) newUser external returns (uint256) {
       userCount += 1;
       userIdsList[msg.sender] = userCount;
       User memory user = User(userCount, _publicKey, false, false, msg.sender );
       userList.push(user);
       //addOnlineUser(userCount);
       userList[userIdsList[msg.sender] - 1].online = true;
       return userCount;
    }

    function connect() oldUser external {
      //  addOnlineUser(userIdsList[msg.sender]);
        userList[userIdsList[msg.sender] - 1].online = true;
    }

    function disconnect() isOnline external {
        removePopUser(userIdsList[msg.sender]);
        userList[userIdsList[msg.sender] - 1].online = false;
        userList[userIdsList[msg.sender] - 1].populated = false;
        requests[msg.sender].length = 0;
        requestsStack[msg.sender] = 0;
    }


    function getUserCount() isOnline view public returns (uint256) {
        return userCount;
    }

    function getAddressOf(uint256 _i) isOnline view public returns (address) {
      return userList[_i - 1].ethAdress;
    }

    function getPublicKeyOf(uint256 _i) isOnline view public returns (string) {
        return userList[_i - 1].publicKey;
    }

    function populated() isOnline external {
        addPopUser(userIdsList[msg.sender]);
        userList[userIdsList[msg.sender] - 1].populated = true;
    }

    function request(address _add, string _encAdd) isOnline external {
        requests[_add].push(_encAdd);
        requestsStack[_add] = requestsStack[_add] + 1;
    }

    modifier userPop(uint256 _userId) {
        require(userList[_userId - 1].populated == true);
        _;
    }

    modifier userNotPop(uint256 _userId) {
        require(userList[_userId - 1].populated == false);
        _;
    }

    function addPopUser(uint256 _userId) userNotPop(_userId) public {
        numberOfPopUsers += 1;

        if (numberOfPopUsers == 1) {
            lastPopUserId = _userId;
            userStructs[_userId] = ListElement(_userId, 0, 0);
        } else {
            userStructs[lastPopUserId].nextUserId = _userId;
            userStructs[_userId] = ListElement(_userId, lastPopUserId, 0);
            lastPopUserId = _userId;
        }
    }

    function removePopUser(uint256 _userId) userPop(_userId) public {

        if (numberOfPopUsers > 1) {

            if (_userId == lastPopUserId) {
                lastPopUserId = userStructs[_userId].previousUserId;
                userStructs[lastPopUserId].nextUserId = 0;
            } else {

                uint256 previousId = getPreviousPopUser(_userId);
                uint256 nextId = getNextPopUser(_userId);

                userStructs[previousId].nextUserId = nextId;
                userStructs[nextId].previousUserId = previousId;

            }
        } else {
            lastPopUserId = 0;
        }

        numberOfPopUsers -= 1;
    }

    function getPreviousPopUser(uint256 _userId) userPop(_userId) public view returns (uint256) {
        return userStructs[_userId].previousUserId;
    }

    function getNextPopUser(uint256 _userId) userPop(_userId) public view returns (uint256) {
        return userStructs[_userId].nextUserId;
    }

   function getLastPopUser() isOnline public view returns (uint256) {
       return lastPopUserId;
   }


}
