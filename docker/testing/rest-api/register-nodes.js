import http from 'k6/http';
import {check} from 'k6';
import exec from 'k6/execution';

export const options = {
    scenarios: {
        register_10_k_nodes: {
            executor: 'shared-iterations',
            vus: 50,
            iterations: 10000,
            maxDuration: '10m',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};


const SERVER_TYPES = [
    'Linux_Server',
    'Windows_Server',
    'Docker_Hosts',
    'Database_Server'
]


const OP_STATUSES = [
    'UP',
    'DOWN',
    'UNREACHABLE',
    'ON_MAINTENANCE'
];



function getRandomElement(array) {
    return array[Math.floor(Math.random() * array.length)];
}



export default function() {
    const index = exec.scenario.iterationInTest;

    const ip3 = Math.floor(index / 256);
    const ip4 = (index % 256);

    const generatedIp = `10.0.${ip3}.${ip4}`;
    const id = index + 1;

    const payload = JSON.stringify({
        name: `dummy_node_${id}`,
        osName: "test-os",
        ipAddress: generatedIp,
        type: getRandomElement(SERVER_TYPES),      // Random selection
        opStatus: getRandomElement(OP_STATUSES),    // Random selection
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${__ENV.JWT_TOKEN}`,
        },
    };

    const url = __ENV.REGISTER_NODES_URL;

    const res = http.post(url, payload, params);

    const isSuccess = check(res, {
        'is status 200 or 201': (r) => r.status === 200 || r.status === 201,
    });

    if (!isSuccess) {
        console.error(
            `\n================ [FAILED CALL #${id}] ================` +
            `\nStatus Code:   ${res.status}` +
            `\nRequest Payload: ${payload}` +
            `\nResponse Body:    ${res.body}` +
            `\n======================================================\n`
        );
    }
}