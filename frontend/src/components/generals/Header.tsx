import React, {useState, useEffect, FC} from 'react';
import { Modal, Input, Row, Col } from 'antd';
import { EnvironmentOutlined, PhoneOutlined, ClockCircleOutlined } from '@ant-design/icons';
import './styles/Header.css';

/**
 * Компонент-header
 * @constructor
 */
const Header: FC = () => {
    const [details, setDetails] = useState<any | null>(null);
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [selectedCity, setSelectedCity] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState<string>('');

    useEffect(() => {
        getUserGeolocationDetails();
    }, []);

    const getUserGeolocationDetails = () => {
        fetch('http://ipwho.is/')
            .then((response) => response.json())
            .then((data) => setDetails(data));
    };

    const openModal = () => {
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
    };

    const handleCitySelect = (city: string) => {
        setSelectedCity(city);
        closeModal();
    };

    const handleSearchQueryChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearchQuery(event.target.value);
    };

    const filteredCities = ['Вологда', 'Череповец', 'Ярославль'].filter((city) =>
        city.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="header">
            <Row gutter={20} align="middle">
                <Col flex="auto">
                    <p className="header__cityLink" onClick={openModal}>
                        <EnvironmentOutlined style={{ fontSize: '16px', paddingRight: '5px' }} />
                        <span>{selectedCity || (details ? details.city : 'Не определен')}</span>
                    </p>
                </Col>
                <Col>
                    <p className="header__phoneNumber">
                        <PhoneOutlined style={{ fontSize: '16px', paddingRight: '5px' }} />
                        Телефон горячей линии: +89218493821
                    </p>
                </Col>
                <Col>
                    <p className="header__workTime">
                        <ClockCircleOutlined style={{ fontSize: '16px', paddingRight: '5px' }} />
                        Сегодня с 9:00 до 00:00
                    </p>
                </Col>
                <Col>
                <p className="header__workTime1">

                    Текущий пользователь: saunindaniil11
                </p>
            </Col>
            </Row>

            <Modal
                title="Выберите город"
                open={isModalOpen}
                onCancel={closeModal}
                footer={null}
            >
                <Input
                    placeholder="Поиск города"
                    value={searchQuery}
                    onChange={handleSearchQueryChange}
                />
                <div style={{ marginTop: '10px' }}>
                    {filteredCities.map((city) => (
                        <p
                            key={city}
                            className="city-link"
                            onClick={() => handleCitySelect(city)}
                        >
                            {city}
                        </p>
                    ))}
                </div>
            </Modal>
        </div>
    );
};

export default Header;
